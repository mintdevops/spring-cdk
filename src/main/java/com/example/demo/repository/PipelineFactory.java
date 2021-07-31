package com.example.demo.repository;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.PipelineConfig;
import com.example.demo.config.VpcConfig;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.SecretValue;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;
import software.amazon.awscdk.pipelines.Step;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
public class PipelineFactory {

    private final static String RESOURCE_NAME = "Pipeline";

    public CodePipeline create(Construct parent, PipelineConfig conf, Environment stage) {
        log.debug("PipelineFactory:create");
        log.debug(stage);
        log.debug(conf);

        // Options configurable ofc, with sensible defaults

        // Based on some configuration we need to produce the build step
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
                .create(parent, RESOURCE_NAME)
                .pipelineName(conf.getName())
                .crossAccountKeys(true)
                .selfMutation(true)
                .synth(synth)
                .publishAssetsInParallel(true)
                .build();
    }

    // standard setters and getters
}