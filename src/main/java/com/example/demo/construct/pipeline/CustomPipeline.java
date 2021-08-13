package com.example.demo.construct.pipeline;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.example.demo.config.GithubConfig;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Arn;
import software.amazon.awscdk.core.ArnComponents;
import software.amazon.awscdk.core.ArnFormat;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.SecretValue;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.RoleProps;
import software.amazon.awscdk.services.iam.ServicePrincipal;

@Getter
@Log4j2
public class CustomPipeline extends Construct {

    private final CodePipeline pipeline; // CDK pipeline not CodePipeline pipeline
    private final Role automationRole;
    private final PolicyStatement assumeRolePolicyStatement;

    public CustomPipeline(software.constructs.@NotNull Construct scope, @NotNull String id, PipelineSpec spec) {
        super(scope, id);

        CodeBuildStep synth = mavenBuildStep(spec.getGithub());

        // Create the pipeline
        // TODO: Allow user configuration
        pipeline = CodePipeline.Builder
                .create(this, id)
                .pipelineName(spec.getPipelineName())
                .crossAccountKeys(true)
                .selfMutation(true)
                .synth(synth)
                .publishAssetsInParallel(true)
                .build();

        // Create a custom role to perform post deployment actions
        // This allows us to run npx awssudo to have a clear security boundary between the pipeline and the environments
        assumeRolePolicyStatement = new PolicyStatement(PolicyStatementProps.builder()
                                                                            .actions(Collections.singletonList(
                                                                                      "sts:AssumeRole"
                                                                              ))
                                                                            .effect(Effect.ALLOW)
                                                                            .build());

        automationRole = new Role(this, "AutomationRole",
                RoleProps
                        .builder()
                        .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                        .build());
    }

    public static List<String> installCommands() {
        return Arrays.asList(
                "env",
                "npm install -g awssudo",
                "pip install jq awscli --upgrade"
        );
    }

    public static List<String> publishAmiIdCommands(String assumeRoleName,
                                                    String ssmParameterName,
                                                                String ssmValueEnvVar
    ) {
        log.debug("{} {} {}", assumeRoleName, ssmParameterName, ssmValueEnvVar);

        // TODO: Validate parameterValueEnvVar exists in envVar

        return Arrays.asList(
                String.format(
                        "npx awssudo $%s aws ssm put-parameter --name %s --type \"String\" --value $%s --overwrite",
                        assumeRoleName,
                        ssmParameterName,
                        ssmValueEnvVar
                )
        );
    }

    public static List<String> distributeAmiIdCommands(String assumeRoleName, String ssmParameterName,
                                                       List<String> accounts, String applicationName, String version) {
        log.debug("{} {} {}", assumeRoleName, ssmParameterName, accounts);

        return Arrays.asList(
                String.format(
                        "ASG=$(aws autoscaling describe-auto-scaling-groups --auto-scaling-group-name $%s)",
                        ssmParameterName
                ),
                "INSTANCE_ID=$(echo $ASG | jq -r '.AutoScalingGroups[0].Instances[0].InstanceId')",
                String.format(
                        "IMAGE_ID=$(aws ec2 create-image --instance-id $INSTANCE_ID --name %s_%s --no-reboot | jq -r '.ImageId')",
                        applicationName,
                        version
                ),
                "aws ec2 wait image-available --image-ids $IMAGE_ID",
                String.format(
                        "aws ec2 modify-image-attribute --image-id $IMAGE_ID --launch-permission \"Add=[%s]\"",
                        accounts.stream().map(s -> String.format("{UserId=%s}", s)).collect(Collectors.joining(","))
                )
        );
    }

    public CodeBuildStep automationStep(Map<String, CfnOutput> envVars, List<String> roleArns,
                                      List<String> commands) {

        log.debug("{} {} {}", envVars, roleArns, commands);

        roleArns.forEach(assumeRolePolicyStatement::addResources);
        automationRole.addToPolicy(assumeRolePolicyStatement);

        List<String> merged =
                Stream.concat(Stream.of(CustomPipeline.installCommands()), Stream.of(commands)).flatMap(Collection::stream).collect(Collectors.toList());

        log.debug(merged);

        CodeBuildStep.Builder builder = CodeBuildStep.Builder.create("Automation")
                                    .role(automationRole)
                                    .commands(merged)
                                    .envFromCfnOutputs(envVars);

        return builder.build();
    }

    // TODO: generic build step to support different CDK languages
    private CodeBuildStep mavenBuildStep(GithubConfig gitSource) {
        // TODO: Add some validation to ensure `setting.xml` exists on classpath first

        String secretArn =
                Arn.format(ArnComponents.builder()
                                        .service("secretsmanager")
                                        .resource("secret").resourceName("MAVEN_PASSWORD-*")
                                        .arnFormat(ArnFormat.COLON_RESOURCE_NAME)
                                        .build(), Stack.of(this));

        // Build the statement
        PolicyStatement statement = new PolicyStatement(PolicyStatementProps.builder()
                                                                            .actions(Arrays.asList(
                                                                                    "secretsmanager:DescribeSecret",
                                                                                    "secretsmanager:GetSecretValue",
                                                                                    "secretsmanager:ListSecretVersionIds",
                                                                                    "secretsmanager:GetResourcePolicy"
                                                                            ))
                                                                            .effect(Effect.ALLOW)
                                                                            .resources(Collections
                                                                                    .singletonList(secretArn))
                                                                            .build());


        // Replace the default role and attach the inline policies
        Role buildRole = new Role(this, "BuildRole",
                RoleProps
                        .builder()
                        .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                        .build());

        buildRole.addToPolicy(statement);

        // FIXME: Find a cleaner way to build the CodeBuild spec
        Map<String, String> envVars = new HashMap<>();
        envVars.put("NPM_CONFIG_UNSAFE_PERM", "true");
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> env = new HashMap<>();
        Map<String, Object> secretsManager = new HashMap<>();
        secretsManager.put("MAVEN_PASSWORD", "MAVEN_PASSWORD");
        env.put("secrets-manager", secretsManager);
        root.put("env", env);

        return CodeBuildStep.Builder
                .create("Synth")
                .input(CodePipelineSource.gitHub(
                        String.format("%s/%s", gitSource.getOwner(), gitSource.getRepo()),
                        gitSource.getBranch(),
                        GitHubSourceOptions.builder()
                                           .authentication(
                                                   SecretValue.secretsManager(gitSource.getToken())
                                           )
                                           .build())
                )
                .role(buildRole)
                .partialBuildSpec(BuildSpec.fromObject(root))
                .env(envVars)
                .commands(Arrays.asList(
                        "node --version",
                        "mvn --version",
                        "npx cdk --version",
                        "cp src/main/resources/settings.xml ~/.m2",
                        "yarn install --frozen-lockfile",
                        "npx cdk synth"
                ))
                .build();
    }
}
