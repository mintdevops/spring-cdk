package com.example.demo.resource;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.stack.LookupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.SecretValue;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineFactory {

    private final static String RESOURCE_NAME = "Pipeline";

    private final AppConfig conf;

    public CodePipeline create(Construct parent, Environment stage) {
        log.debug("create");

        CodeBuildStep synth = CodeBuildStep.Builder
                .create("Synth")
                .input(CodePipelineSource.gitHub(String.format("%s/%s", conf.getPipeline()
                        .getGithub()
                        .getOwner(), conf.getPipeline()
                        .getGithub()
                        .getRepo()), conf.getPipeline()
                        .getGithub()
                        .getBranch(), GitHubSourceOptions
                        .builder()
                        .authentication(SecretValue.secretsManager(conf.getPipeline()

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
                .create(parent, Label.builder()
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

    // standard setters and getters
}