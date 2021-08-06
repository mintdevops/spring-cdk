package com.example.demo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.PipelineConfig;
import com.example.demo.config.LookupType;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.SecretValue;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineRepository extends AbstractResourceRepository<CodePipeline, PipelineConfig> {

    private final static String RESOURCE_NAME = "Pipeline";

    @Override
    public CodePipeline create(Construct scope, String namespace, Environment stage, PipelineConfig conf) {
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
                //.role(maven)
                //.partialBuildSpec(BuildSpec.fromObject(root))
                //.env(envVars)
                .commands(Arrays.asList(
                        "node --version",
                        "mvn --version",
                        "npx cdk --version",
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
