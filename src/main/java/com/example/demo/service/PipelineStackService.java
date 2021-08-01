package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.StackType;
import com.example.demo.resource.PipelineFactory;
import com.example.demo.resource.StackFactory;
import com.example.demo.resource.StageFactory;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.StageDeployment;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Getter
@Setter
public class PipelineStackService implements IStack {

    private static final StackType QUALIFIER = StackType.PIPELINE;

    private final Root root;
    private final AppConfig config;
    private final StackFactory stackFactory;
    private final PipelineFactory pipelineFactory;
    private final StageFactory stageFactory;
    private final TaggingService taggingService;

    private Construct scope;
    private Stack stack;
    private Environment env = Environment.CICD;
    private String namespace;
    private Map<String, String> tags = new HashMap<>();

    @Override
    public void setScope(Construct scope) {
        this.scope = scope;
    }

    @PostConstruct
    public void provision() {
        log.debug("provision");

        stack = stackFactory.create(scope == null ? root.getRootScope() : scope, config.getName(),
                config.getPipeline().getDeploy().getAccount(), config.getPipeline().getDeploy().getRegion());

        CodePipeline pipeline = addPipeline();

        for (Environment env : config.getPipeline().getEnvironments()) {
            StageDeployment envStage = addDeployStage(pipeline, env);
        }

        pipeline.buildPipeline();

        taggingService.addEnvironmentTags(stack, env, QUALIFIER.name());
        taggingService.addTags(stack, config.getPipeline().getTags(), QUALIFIER.name());
    }

    private CodePipeline addPipeline() {
        log.debug("addPipeline");

        return pipelineFactory.create(stack, env);
    }

    private StageDeployment addDeployStage(CodePipeline pipeline, Environment env) {
        log.debug("addDeployStage");

        Stage stage = stageFactory.create(pipeline, env);

        return pipeline.addStage(stage);
    }

    public String getQualifier() {
        return PipelineStackService.QUALIFIER.name();
    }

}
