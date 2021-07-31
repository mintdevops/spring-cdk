package com.example.demo.svc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.Label;
import com.example.demo.config.StackType;
import com.example.demo.config.StageConfig;
import com.example.demo.repository.PipelineFactory;
import com.example.demo.repository.StageFactory;

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
@Setter
public class PipelineStackService implements IStack {

    private final Root root;
    private final AppConfig config;
    private final PipelineFactory pipelineFactory;
    private final StageFactory stageFactory;

    private Construct scope;
    private Stack stack;
    private Environment env = Environment.DEV;
    private String namespace;

    @Override
    public void setScope(Construct scope) {
        this.scope = scope;
    }

    @PostConstruct
    public void provision() {
        log.debug("provision");

        stack = Stack.Builder.create(root.getRootScope(), config.getName())
                             .build();

        CodePipeline pipeline = addPipeline();

        for (Environment env :config.getPipeline().getEnvironments()) {
            StageDeployment envStage = addDeployStage(pipeline, env);
        }
    }

    private CodePipeline addPipeline() {
        log.debug("addPipeline");

        return pipelineFactory.create(stack, config.getPipeline(), env);
    }

    private StageDeployment addDeployStage(CodePipeline pipeline, Environment env) {
        log.debug("addDeployStage");

        Stage stage = stageFactory.create(pipeline, env);

        return pipeline.addStage(stage);
    }

}
