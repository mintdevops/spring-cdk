package com.example.demo.resource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.Label;
import com.example.demo.config.StackType;
import com.example.demo.stack.ImageStackService;
import com.example.demo.stack.LookupService;
import com.example.demo.stack.NetworkStackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stage;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StageFactory {

    private final static String RESOURCE_NAME = "Stage";

    private final AppConfig conf;
    private final LookupService lookupService;
    private final NetworkStackService networkStackService;
    private final ImageStackService imageStackService;

    private final Map<StackType, IStack> stackMap = new HashMap<>();

    @PostConstruct
    public void registerStacks() {
        stackMap.put(StackType.NETWORK, networkStackService);
        stackMap.put(StackType.IMAGE, imageStackService);
    }

    public Stage create(Construct parent, Environment stage) {
        log.debug("create");

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

        Stage stg = Stage.Builder.create(parent,
                Label.builder()
                     .namespace("")
                     .stage(stage.toString())
                     .resource(RESOURCE_NAME)
                     .build()
                     .toString())
                                 .env(env)
                                 .build();

        IStack stack = stackMap.get(conf.getPipeline().getStack());

        log.debug("Adding stack to stage {}", stg.getStageName());

        stack.setNamespace("Infra"); // Stack resources have the same name otherwise
        stack.setScope(stg);
        stack.setEnv(stage);
        stack.provision();

        return stg;
    }

    // standard setters and getters
}