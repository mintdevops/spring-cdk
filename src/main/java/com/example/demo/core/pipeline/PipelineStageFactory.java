package com.example.demo.core.pipeline;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.StackType;
import com.example.demo.service.IStackService;
import com.example.demo.service.ImageStackService;
import com.example.demo.service.NetworkStackService;
import com.example.demo.service.TaggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stage;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineStageFactory {

    private final static String RESOURCE_NAME = "Stage";

    private final AppConfig conf;
    private final TaggingService taggingService;
    private final NetworkStackService networkStackService;
    private final ImageStackService imageStackService;

    private final Map<StackType, IStackService> stackMap = new HashMap<>();

    @PostConstruct
    public void registerStacks() {
        stackMap.put(StackType.NETWORK, networkStackService);
        stackMap.put(StackType.IMAGE, imageStackService);

        // TODO: Support user provided stacks
    }

    public Stage create(Construct parent, String namespace, Environment stage) {
        log.debug("create");

        if (conf.getEnv().get(stage) == null) {
            log.error("Environment {} enabled but not config found", stage.name());

            throw new IllegalStateException();
        }

        String envAccount = conf.getEnv().get(stage).getDeploy().getAccount();
        String envRegion = conf.getEnv().get(stage).getDeploy().getRegion();
        String pipelineAccount = conf.getPipeline().getDeploy().getAccount();
        String pipelineRegion = conf.getPipeline().getDeploy().getRegion();

        software.amazon.awscdk.core.Environment env;
        if (!envAccount.isEmpty() && !envRegion.isEmpty()) {
            env = software.amazon.awscdk.core.Environment.builder().account(envAccount).region(envRegion).build();
        }
        else if (!pipelineAccount.isEmpty() && !pipelineRegion.isEmpty()) {
            env = software.amazon.awscdk.core.Environment.builder().account(pipelineAccount).region(pipelineRegion).build();
        }
        else {
            env = software.amazon.awscdk.core.Environment.builder().account(System.getenv(
                    "CDK_DEFAULT_ACCOUNT")).region(System.getenv("CDK_DEFAULT_REGION")).build();
        }

        Stage pipelineStage = Stage.Builder.create(parent,
                Label.builder()
                     .namespace("")
                     .stage(stage.toString())
                     .resource(RESOURCE_NAME)
                     .build()
                     .toString())
                                 .env(env)
                                 .build();

        IStackService stackService = stackMap.get(conf.getPipeline().getStack());

        log.debug("Adding stack {} to stage {}", conf.getPipeline().getStack(), pipelineStage.getStageName());

        stackService.provision(pipelineStage, namespace, stage);

        taggingService.addEnvironmentTags(pipelineStage, stage, conf.getPipeline().getStack().toString());

        return pipelineStage;
    }

}