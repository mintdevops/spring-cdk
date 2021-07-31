package com.example.demo.repository;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.StageConfig;
import com.example.demo.config.VpcConfig;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Stage;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
public class StageFactory {

    private final static String RESOURCE_NAME = "Stage";

    public Stage create(Construct parent, StageConfig conf, Environment stage) {
        log.debug("StageFactory:create");
        log.debug(stage);
        log.debug(conf);

        Stage stg = Stage.Builder.create(parent, RESOURCE_NAME).build();

        for (IStack stack : conf.getStacks() ) {
            log.debug("Adding stack to stage {}", stg.getStageName());

            stack.setScope(stg);
            stack.setEnvironment(stage);
            stack.provision();
        }

        return stg;
    }

    // standard setters and getters
}