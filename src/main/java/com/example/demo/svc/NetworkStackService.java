package com.example.demo.svc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.repository.VpcFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
public class NetworkStackService implements IStack {

    @Autowired
    Root root;

    @Autowired
    AppConfig config;

    @Autowired
    VpcFactory vpcFactory;

    // We dont really know where the resources in this service would end up in the construct tree
    Construct scope;
    Stack stack;

    public void setScope(Construct scope) {
        this.scope = scope;
    }

    public void provision() {
        log.debug("NetworkStackService:provision");
        log.debug(config);

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope).build();

        Vpc vpc = addPublicPrivateIsolatedVpc();
    }

    private Vpc addPublicPrivateIsolatedVpc() {
        // Perform any resource specific business logic here e.g. add nat gateway alarm

        return vpcFactory.create(stack, config.getVpc(), Environment.DEV);
    }

}
