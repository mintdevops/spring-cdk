package com.example.demo.svc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.StackType;
import com.example.demo.config.StageConfig;
import com.example.demo.repository.PipelineFactory;
import com.example.demo.repository.StageFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.StageDeployment;

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

    @Autowired
    ImageStackService imageStackService;

    private final Map<String, IStack> serviceMap = new HashMap<>();

    Construct scope;
    Stack stack;
    Environment env = Environment.DEV;

    @Override
    public void setScope(Construct scope) {
        this.scope = scope;
    }

    @PostConstruct
    public void provision() {
        log.debug("PipelineStackService:provision");
        log.debug(config);

        serviceMap.put(StackType.NETWORK.toString(), networkStackService);
        serviceMap.put(StackType.IMAGE.toString(), imageStackService);

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

        log.debug("addDeployStage");
        log.debug(config.getEnv());

        conf.getStacks().add(serviceMap.get(config.getPipeline().getStack().toString()));

        Stage stage = stageFactory.create(pipeline, conf, env);

        return pipeline.addStage(stage);
    }

    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
    }
}
