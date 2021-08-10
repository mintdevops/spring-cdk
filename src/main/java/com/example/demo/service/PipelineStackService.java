package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.StackType;
import com.example.demo.repository.PipelineRepository;
import com.example.demo.core.pipeline.StackFactory;
import com.example.demo.core.pipeline.PipelineStageFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Arn;
import software.amazon.awscdk.core.ArnComponents;
import software.amazon.awscdk.core.ArnFormat;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.ManualApprovalStep;
import software.amazon.awscdk.pipelines.StageDeployment;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.RoleProps;
import software.amazon.awscdk.services.iam.ServicePrincipal;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineStackService extends AbstractStackService {

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final PipelineStageFactory stageFactory;
    private final TaggingService taggingService;
    private final PipelineRepository pipelineRepository;
    private final PipelineDeployStackService pipelineDeployStackService;
    private final StackOutputService stackOutputService;


    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, config.getName(),
                config.getPipeline().getDeploy().getAccount(), config.getPipeline().getDeploy().getRegion());

        CodePipeline pipeline = addPipeline(stack, stage);

        for (Environment env : config.getPipeline().getEnvironments()) {
            StageDeployment confStage = addConfDeploy(pipeline, env);
            StageDeployment infraStage = addInfraDeploy(pipeline, env);

            switch (config.getPipeline().getStack()) {
                case IMAGE:
                    addPublishAmiToSsmStep(stack, confStage, infraStage, env);
                    break;
                case WORKLOAD:
                    addDistributeAmiStep(stack, confStage, infraStage, env);
                    break;
            }

            if (Environment.PROD == env) {
                infraStage.addPost(new ManualApprovalStep("Approval"));
            }
        }

        pipeline.buildPipeline();

        taggingService.addEnvironmentTags(stack, stage, StackType.PIPELINE.name());
        taggingService.addTags(stack, config.getPipeline().getTags(), StackType.PIPELINE.name());

        return stack;
    }

    private CodePipeline addPipeline(Stack stack, Environment stage) {
        log.debug("addPipeline");

        return pipelineRepository.create(stack, "", stage, config.getPipeline());
    }

    private StageDeployment addConfDeploy(CodePipeline pipeline, Environment env) {
        log.debug("addConfStage");

        Stage stage = stageFactory.create(pipeline, "Conf", env, pipelineDeployStackService);

        return pipeline.addStage(stage);
    }

    private StageDeployment addInfraDeploy(CodePipeline pipeline, Environment env) {
        log.debug("addDeployStage");

        Stage stage = stageFactory.create(pipeline, "Infra", env);

        return pipeline.addStage(stage);
    }

    private void addPublishAmiToSsmStep(Stack stack, StageDeployment confStage, StageDeployment infraStage,
                                        Environment stage) {
        log.debug("addPublishAmiToSsmStep {} {}", confStage.getStageName(), infraStage.getStageName());

        String roleArn =
                Arn.format(ArnComponents.builder()
                                        .service("iam")
                                        .account("")
                                        .region("")
                                        .resource("role").resourceName(Label.builder()
                                                                              .resource("PutSsmParameter")
                                                                              .namespace(config.getName())
                                                                              .stage(stage.name())
                                                                              .build().toString())
                                        .arnFormat(ArnFormat.SLASH_RESOURCE_NAME)
                                        .build(), Stack.of(stack));


        PolicyStatement statement = new PolicyStatement(PolicyStatementProps.builder()
                                                .actions(Collections.singletonList(
                                                        "sts:AssumeRole"
                                                ))
                                                .effect(Effect.ALLOW)
                                                .resources(
                                                        Arrays.asList(roleArn)
                                                )
                                                .build());

        Role publishRole = new Role(stack,
                Label.builder().namespace(config.getName()).stage(stage.name()).resource("AmiPublish").build().toString(),
                RoleProps
                        .builder()
                        .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                        .build());

        publishRole.addToPolicy(statement);

        infraStage.addPost(
                CodeBuildStep.Builder.create("Publish")
                                    .envFromCfnOutputs(stackOutputService.toEnvVars(stage))
                                    .role(publishRole)
                                    .commands(Arrays.asList(
                                       "env",
                                       "npm install -g awsudo",
                                       "pip install jq awscli --upgrade",
                                       // foo="/ConfDevStage-MyApplication/RoleArn"
                                       // bar="StackName": "InfraDevStage-MyApplication",
                                       // baz=CONFDEVSTAGE_MYAPPLICATION_ROLEARN
                                       String.format(
                                               "npx awsudo $%s " +
                                               "aws ssm put-parameter --name /%s/AmiId --type \"String\" " +
                                               "--value %s --overwrite",
                                               roleArn,
                                               String.format("%s-%s", infraStage.getStageName(), config.getName()),
                                               String.format("CONF%STAGE_%s_PUBLISHSSMROLEARN",
                                                       stage.name(),
                                                       config.getName().toUpperCase())
                                       )
                                    )).build());
    }

    private void addDistributeAmiStep(Stack stack, StageDeployment confStage, StageDeployment infraStage,
                                        Environment stage) {
        log.debug("addPublishAmiToSsmStep {} {}", confStage.getStageName(), infraStage.getStageName());

        String roleArn =
                Arn.format(ArnComponents.builder()
                                        .service("iam")
                                        .account("")
                                        .region("")
                                        .resource("role").resourceName(Label.builder()
                                                                            .resource("CreateEc2Image")
                                                                            .namespace(config.getName())
                                                                            .stage(stage.name())
                                                                            .build().toString())
                                        .arnFormat(ArnFormat.SLASH_RESOURCE_NAME)
                                        .build(), Stack.of(stack));


        PolicyStatement statement = new PolicyStatement(PolicyStatementProps.builder()
                                                                            .actions(Collections.singletonList(
                                                                                    "sts:AssumeRole"
                                                                            ))
                                                                            .effect(Effect.ALLOW)
                                                                            .resources(
                                                                                    Arrays.asList(roleArn)
                                                                            )
                                                                            .build());

        Role createRole = new Role(stack,
                Label.builder().namespace(config.getName()).stage(stage.name()).resource("AmiCreate").build().toString(),
                RoleProps
                        .builder()
                        .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                        .build());

        createRole.addToPolicy(statement);

        infraStage.addPost(
                CodeBuildStep.Builder.create("Create")
                                     .envFromCfnOutputs(stackOutputService.toEnvVars(stage))
                                     .role(createRole)
                                     .commands(Arrays.asList(
                                             "env",
                                             "npm install -g awsudo",
                                             "pip install jq awscli --upgrade",
                                             "ASG=$(aws autoscaling describe-auto-scaling-groups " +
                                                     "--auto-scaling-group-name " +
                                                     "$TMSAPPLICATIONDEVSTAGE_INFRA_ASGNAME)",
                                             "INSTANCE_ID=$(echo $ASG | jq -r '.AutoScalingGroups[0]" +
                                                     ".Instances[0].InstanceId')",
                                             String.format("IMAGE_ID=$(aws ec2 create-image --instance-id " +
                                                             "$INSTANCE_ID " +
                                                             "--name %s_%s --no-reboot | jq -r '.ImageId')",
                                                     config.getArtifact().getArtifactName(),
                                                     props.get("git.commit.id.abbrev")),
                                             "aws ec2 wait image-available --image-ids $IMAGE_ID",
                                             String.format("aws ssm put-parameter --name /%s-Infra/AmiId " +
                                                     "--type " +
                                                     "\"String\" " +
                                                     "--value $IMAGE_ID --overwrite", stageName),
                                             String.format("aws ec2 modify-image-attribute --image-id " +
                                                             "$IMAGE_ID" +
                                                             " --launch-permission \"Add=[%s]\"",
                                                     share.stream().map(s -> String.format("{UserId=%s}", s)).collect(Collectors
                                                             .joining(","))),
                                             String.format("npx awsudo $TMSAPPLICATIONTESTCONFSTAGE_ROLEARN " +
                                                     "aws ssm put-parameter --name /%s-Infra/AmiId --type " +
                                                     "\"String\" " +
                                                     "--value $IMAGE_ID --overwrite", stageName)
                                     )).build());
    }

}
