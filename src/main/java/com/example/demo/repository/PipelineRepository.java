package com.example.demo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.PipelineConfig;
import com.example.demo.config.LookupType;

import lombok.RequiredArgsConstructor;
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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineRepository extends AbstractResourceRepository<CodePipeline, PipelineConfig> {

    private final static String RESOURCE_NAME = "Pipeline";

    @Override
    public CodePipeline create(Construct scope, String namespace, Environment stage, PipelineConfig conf) {
        String secretArn =
                Arn.format(ArnComponents.builder()
                                        .service("secretsmanager")
                                        .resource("secret").resourceName("MAVEN_PASSWORD-*")
                                        .arnFormat(ArnFormat.COLON_RESOURCE_NAME)
                                        .build(), Stack.of(scope));

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
        Role buildRole = new Role(scope, "BuildRole",
                RoleProps
                        .builder()
                        .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                        .build());

        buildRole.addToPolicy(statement);

        Map<String, String> envVars = new HashMap<>();
        envVars.put("NPM_CONFIG_UNSAFE_PERM", "true");
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> env = new HashMap<>();
        Map<String, Object> secretsManager = new HashMap<>();
        secretsManager.put("MAVEN_PASSWORD", "MAVEN_PASSWORD");
        env.put("secrets-manager", secretsManager);
        root.put("env", env);

        CodeBuildStep synth = CodeBuildStep.Builder
                .create("Synth")
                .input(CodePipelineSource.gitHub(String.format("%s/%s", conf
                        .getGithub()
                        .getOwner(), conf
                        .getGithub()
                        .getRepo()), conf
                        .getGithub()
                        .getBranch(), GitHubSourceOptions
                        .builder()
                        .authentication(SecretValue.secretsManager(conf

                                .getGithub()
                                .getToken()))
                        .build()))
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

        return CodePipeline.Builder
                .create(scope, Label.builder()
                                    .namespace("")
                                    .stage("")
                                    .resource(RESOURCE_NAME)
                                    .build()
                                    .toString())
                .pipelineName(conf.getName())
                .crossAccountKeys(true)
                .selfMutation(true)
                .synth(synth)
                .publishAssetsInParallel(true)
                .build();
    }

    @Override
    public CodePipeline lookup(Construct scope, String stackName, LookupType lookupType) {
        throw new NotImplementedException();
    }

    public List<CfnOutput> export(Construct scope, CodePipeline resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "PipelineArn", "The Pipelien ARN", resource.getPipeline().getPipelineArn()));

        return outputs;
    }
}
