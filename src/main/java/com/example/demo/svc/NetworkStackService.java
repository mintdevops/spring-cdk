package com.example.demo.svc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.Label;
import com.example.demo.repository.VpcFactory;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
@Setter
public class NetworkStackService implements IStack {

    private final static String RESOURCE_NAME = "Network";

    @Autowired
    Root root;

    @Autowired
    AppConfig config;

    @Autowired
    VpcFactory vpcFactory;

    Construct scope;
    Stack stack;
    Environment env = Environment.DEV;
    String namespace;

    public void setScope(Construct scope) {
        this.scope = scope;
    }

    public void provision() {
        log.debug("provision");

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope)
//                             .stackName(
//                                     Label.builder()
//                                          .namespace("")
//                                          .stage(env.toString())
//                                          .resource(RESOURCE_NAME)
//                                          .build()
//                                          .toString()
//                             )
                             .build();

        Vpc vpc = addPublicPrivateIsolatedVpc();
    }

    private Vpc addPublicPrivateIsolatedVpc() {
        log.debug("addPublicPrivateIsolatedVpc");

        // Perform any resource specific business logic here e.g. add nat gateway alarm

        return vpcFactory.create(stack, env);
    }

}
