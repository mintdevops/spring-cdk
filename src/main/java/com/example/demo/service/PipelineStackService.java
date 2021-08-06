package com.example.demo.service;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.StackType;
import com.example.demo.repository.PipelineRepository;
import com.example.demo.core.pipeline.StackFactory;
import com.example.demo.core.pipeline.PipelineStageFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.StageDeployment;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineStackService extends AbstractStackService {

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final PipelineStageFactory stageFactory;
    private final TaggingService taggingService;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageService pipelineStageService;


    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, config.getName(),
                config.getPipeline().getDeploy().getAccount(), config.getPipeline().getDeploy().getRegion());

        CodePipeline pipeline = addPipeline(stack, stage);

        for (Environment env : config.getPipeline().getEnvironments()) {
            StageDeployment envStage = addInfraDeploy(pipeline, env);

            addValidationStep(envStage);
        }

        pipeline.buildPipeline();

        taggingService.addEnvironmentTags(stack, stage, StackType.PIPELINE.name());
        taggingService.addTags(stack, config.getPipeline().getTags(), StackType.PIPELINE.name());

        return stack;
    }

    private CodePipeline addPipeline(Stack stack, Environment stage) {
        log.debug("addPipeline");

        return pipelineRepository.create(stack, "",stage, config.getPipeline());
    }

    private StageDeployment addInfraDeploy(CodePipeline pipeline, Environment env) {
        log.debug("addDeployStage");

        Stage stage = stageFactory.create(pipeline, "Infra", env);

        return pipeline.addStage(stage);
    }

    private void addValidationStep(StageDeployment stage) {
        stage.addPost(
                CodeBuildStep.Builder.create("Validate")
                                           .envFromCfnOutputs(pipelineStageService.getOutputs())
                                           .commands(Arrays.asList(
                                                   "env"
                                           )).build());
    }

}
