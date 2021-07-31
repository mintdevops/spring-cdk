package com.example.demo.repository;

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
import com.example.demo.svc.ImageStackService;
import com.example.demo.svc.NetworkStackService;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stage;

@Component
@Log4j2
public class StageFactory {

    private final static String RESOURCE_NAME = "Stage";
    private final Map<StackType, IStack> stackMap = new HashMap<>();
    @Autowired
    AppConfig conf;
    @Autowired
    NetworkStackService networkStackService;
    @Autowired
    ImageStackService imageStackService;

    @PostConstruct
    public void initStackMap() {
        stackMap.put(StackType.NETWORK, networkStackService);
        stackMap.put(StackType.IMAGE, imageStackService);
    }

    public Stage create(Construct parent, Environment stage) {
        log.debug("create");

        Stage stg = Stage.Builder.create(parent,
                Label.builder()
                     .namespace("")
                     .stage(stage.toString())
                     .resource(RESOURCE_NAME)
                     .build()
                     .toString())
                                 .build();

        IStack stack = stackMap.get(conf.getPipeline().getStack());

        log.debug("Adding stack to stage {}", stg.getStageName());

        stack.setNamespace("Infra");
        stack.setScope(stg);
        stack.setEnv(stage);
        stack.provision();

        return stg;
    }

    // standard setters and getters
}