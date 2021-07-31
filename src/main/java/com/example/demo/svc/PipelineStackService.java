package com.example.demo.svc;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.StageConfig;
import com.example.demo.repository.PipelineFactory;
import com.example.demo.repository.StageFactory;
import com.example.demo.repository.VpcFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.SecretValue;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;
import software.amazon.awscdk.pipelines.StageDeployment;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
public class PipelineStackService implements IStack {

    @Autowired
    Root root;

    @Autowired
    AppConfig config;

    @Autowired
    PipelineFactory pipelineFactory;

    @Autowired
    StageFactory stageFactory;

    @Autowired
    NetworkStackService networkStackService;

    Construct scope;
    Stack stack;

    @Override
    public void setScope(Construct scope) {
        this.scope = scope;
    }

    @PostConstruct
    public void provision() {
        log.debug("PipelineStackService:provision");
        log.debug(config);

        stack = Stack.Builder.create(root.getRootScope()).build();

        CodePipeline pipeline = addPipeline();
        StageDeployment dev = addDeployStage(pipeline, Environment.DEV);
    }

    private CodePipeline addPipeline() {
        // Create the basic pipeline structure
        return pipelineFactory.create(stack, config.getPipeline(), Environment.PROD);
    }

    private StageDeployment addDeployStage(CodePipeline pipeline, Environment env) {
        StageConfig conf = new StageConfig();

        // TODO: How to make this generic without having a pipeline service for every stack it _can_ deploy
        conf.getStacks().add(networkStackService);

        Stage stage = stageFactory.create(pipeline, conf, env);

        return pipeline.addStage(stage);
    }

}
