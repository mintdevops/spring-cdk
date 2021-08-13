package com.example.demo.service.cloudformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.construct.pipeline.CustomPipeline;
import com.example.demo.construct.pipeline.PipelineSpec;
import com.example.demo.core.Environment;
import com.example.demo.core.StackFactory;
import com.example.demo.core.StackType;
import com.example.demo.core.StageFactory;
import com.example.demo.repository.PipelineRepository;
import com.example.demo.service.AbstractInfrastructureService;
import com.example.demo.service.IInfrastructureService;
import com.example.demo.service.TaggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Arn;
import software.amazon.awscdk.core.ArnComponents;
import software.amazon.awscdk.core.ArnFormat;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.pipelines.ManualApprovalStep;
import software.amazon.awscdk.pipelines.StageDeployment;
import software.amazon.awscdk.pipelines.Wave;

/**
 * A service to provision a Cloudformation stack to manage a CodePipeline definition.
 *
 * The stacks produced by this service are used to continuously deploy CDK applications.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineStackService extends AbstractInfrastructureService {

    private final AppConfig conf;
    private final StackFactory stackFactory;
    private final StageFactory stageFactory;
    private final TaggingService taggingService;
    private final StackOutputService stackOutputService;
    private final PipelineRepository pipelineRepository;
    private final AutomationStackService automation;
    private final NetworkStackService networkStackService;
    private final ImageStackService imageStackService;
    private final AppStackService appStackService;
    private final AutomationStackService automationStackService;

    private final Map<StackType, IInfrastructureService> stackMap = new HashMap<>();

    @PostConstruct
    public void registerStacks() {
        stackMap.put(StackType.NETWORK, networkStackService);
        stackMap.put(StackType.IMAGE, imageStackService);
        stackMap.put(StackType.WORKLOAD, appStackService);

        // TODO: Support user provided stacks probably via annotation
    }

    public Stack provision(Construct scope, String namespace, Environment stage) {
        // NB: Pipeline stacks need to know where they are deployed to manage cross account/regional deployments
        Stack stack = stackFactory.create(
                scope,
                conf.getApplicationName(),
                stage,
                conf.getPipeline().getDeploy().getAccount(),
                conf.getPipeline().getDeploy().getRegion()
        );

        // Create the source/synth stage
        CustomPipeline pipeline =  pipelineRepository.create(stack, "", stage, PipelineSpec.builder()
                                                                                                     .pipelineName(conf.getApplicationName())
                                                                                                     .github(conf.getPipeline().getGithub())
                                                                                                     .build());

        // For each of the configured environments provision a deployment stage
        // We deploy an automation stack before the actual environments so post deployment steps can utilize resources
        // provisioned by the stack. This keeps the CDK code for the environment lean.
        // The automation stack can be deployed in parallel to each of the environments (typically separate accounts).
        Wave w = pipeline.getPipeline().addWave("Automation");
        for (Environment env : conf.getPipeline().getEnvironments()) {
            Stage automationStage = stageFactory.create(pipeline, "Automation", env, automation);
            Stage infraStage = stageFactory.create(pipeline, "Infra", env, stackMap.get(StackType.valueOf(conf.getPipeline().getStack())));
            StageDeployment automation = w.addStage(automationStage);
            StageDeployment infra = pipeline.getPipeline().addStage(infraStage);

            // Control the post-deployment behaviour of each stack archetype
            // This is series of CodeBuild/Lambda steps (Lambda for long running async processes)
            // We cannot pass resource attributes (e.g. Iam role ARN) up the construct tree (from a deployed stack to
            // the pipeline) so instead we must coalesce the values from the automation stack so we can grant the
            // execution permissions to assume the roles to perform the actions.
            // We can however pass the CfnOutputs of the deployed stacks downstream as environment variables which
            // simplifies step execution as we do not have to lookup these outputs using the CloudFormation API. For
            // CodeBuild these values are available in the Shell environment and for Lambda these values are
            // available in the payload under UserParameters.
            switch (StackType.valueOf(conf.getPipeline().getStack())) {
                case IMAGE:
                    // Only publish in production (reduce costs)
                    if (Environment.PROD == env) {
                        log.info("Adding publish step for {}", StackType.IMAGE.name());

                        // Push the ami id produced by the image stack to SSM parameter store
                        List<String> publishCommands = CustomPipeline.publishAmiIdCommands(
                                coalesceEnvVar(
                                        automation.getStacks().get(0).getStackName(),
                                        automationStackService.getPublishAmiIdRoleOutputName()
                                ),
                                coalesceParameterName(
                                        infra.getStacks().get(0).getStackName(),
                                        "AmiId"
                                ),
                                coalesceEnvVar(
                                        infra.getStacks().get(0).getStackName(),
                                        "AmiId"
                                )
                        );
                        infra.addPost(pipeline.automationStep(
                                stackOutputService.toEnvVars(env),
                                Collections.singletonList(
                                        coalesceIamRoleArn(stack, automation.getStacks().get(0).getStackName(),
                                                automationStackService.getPublishAmiIdRoleName())
                                ),
                                publishCommands
                        ));
                    }
                    break;
                case WORKLOAD:
                    // Only publish in development (other environments use this value in their templates)
                    if (Environment.DEV == env) {
                        // Create an image and share it with other AWS accounts
                        // NB: This is effectively the same as the ImageBuilder construct but far quicker
                        List<String> distributeCommands = CustomPipeline.distributeAmiIdCommands(
                                coalesceEnvVar(
                                    automation.getStacks().get(0).getStackName(),
                                    automationStackService.getCreateEc2ImageRoleOutputName()
                                ),
                                coalesceEnvVar(
                                        infra.getStacks().get(0).getStackName(),
                                        "AsgName"
                                ),
                                conf.getPipeline().getEnvironments()
                                    .stream()
                                    .map(environment -> conf.getEnv().get(environment).getDeploy().getAccount())
                                    .filter(s -> !s.isEmpty())
                                    .distinct()
                                    .collect(Collectors.toList()),
                                conf.getApplicationName(),
                                conf.getVersion()
                        );
                        // Push the ami id produced by the image stack to SSM parameter store
                        List<String> publishCommands = CustomPipeline.publishAmiIdCommands(
                                coalesceEnvVar(
                                        automation.getStacks().get(0).getStackName(),
                                        automationStackService.getPublishAmiIdRoleOutputName()
                                ),
                                coalesceParameterName(
                                        infra.getStacks().get(0).getStackName(),
                                        "AmiId"
                                ),
                                coalesceEnvVar(
                                        infra.getStacks().get(0).getStackName(),
                                        "AmiId"
                                )
                        );
                        infra.addPost(pipeline.automationStep(
                                stackOutputService.toEnvVars(env),
                                Arrays.asList(coalesceIamRoleArn(stack,  automation.getStacks().get(0).getStackName()
                                        , automationStackService.getPublishAmiIdRoleName()),
                                        coalesceIamRoleArn(stack,  automation.getStacks().get(0).getStackName(),
                                                automationStackService.getCreateEc2ImageRoleName())),
                                Stream.concat(distributeCommands.stream(), publishCommands.stream())
                                      .collect(Collectors.toList())
                        ));
                    }
                    if (Environment.PROD == env) {
                        infra.addPost(new ManualApprovalStep("Approval"));
                    }
                    break;
            }
        }

        // NB: Only call after entire pipeline has been defined
        pipeline.getPipeline().buildPipeline();

        taggingService.addEnvironmentTags(stack, stage, StackType.PIPELINE.name());
        taggingService.addTags(stack, conf.getPipeline().getTags(), StackType.PIPELINE.name());

        return stack;
    }

    /**
     * Figure out the IAM role ARN from the stack in which it was produced.
     *
     * @param stack
     * @param stackName
     * @param roleName
     * @return
     */
    private String coalesceIamRoleArn(Stack stack, String stackName, String roleName) {
        return Arn.format(ArnComponents.builder()
                 .service("iam")
                 .account("")
                 .region("")
                 .resource("role")
                 .resourceName(String.format("%s-%s", stackName, roleName))
                 .arnFormat(ArnFormat.SLASH_RESOURCE_NAME)
                 .build(), Stack.of(stack));
    }

    /**
     * Figure out the name of the environment variable from the stack in which it was produced and the name of the
     * CfnOutput.
     *
     * @param stackName
     * @param outputName
     * @return
     */
    private String coalesceEnvVar(String stackName, String outputName) {
        return String.format("%s_%s", stackName, outputName).replaceAll("-", "_").toUpperCase();
    }

    /**
     * Figure out the name of the SSM parameter from the stack in which it should be produced and the name of the
     * parameter.
     *
     * @param stackName
     * @param paramName
     * @return
     */
    private String coalesceParameterName(String stackName, String paramName) {
        return String.format("/%s/%s", stackName, paramName);
    }

}
