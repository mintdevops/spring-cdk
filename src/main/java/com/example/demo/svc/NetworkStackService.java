package com.example.demo.svc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.repository.VpcFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

@Component
@Log4j2
public class NetworkStackService {

    // Main components autowired
    // No heavy constructors

    @Autowired
    Root root;
    Stack stack;

    @Autowired
    AppConfig config;

    @Autowired
    VpcFactory vpcFactory;

    @PostConstruct
    public void provision() {
        log.debug("NetworkStackService:provision");
        log.debug(config);

        // We need a stack in the stack service to attach resources to
        stack = Stack.Builder.create(root.getRootScope()).build();

        Construct c = addPublicPrivateIsolatedVpc();
    }

    private Construct addPublicPrivateIsolatedVpc() {
        // Perform any resource specific business logic here e.g. add nat gateway alarm

        return vpcFactory.create(stack);
    }

}
