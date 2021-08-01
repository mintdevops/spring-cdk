package com.example.demo.stack;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.StackType;
import com.example.demo.resource.StackFactory;
import com.example.demo.resource.VpcFactory;
import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Setter
public class NetworkStackService implements IStack {

    private static final StackType QUALIFIER = StackType.NETWORK;
    private static final Map<String, String> TAGS;

    static {
        TAGS = new HashMap<>();
    }

    private final Root root;
    private final AppConfig config;
    private final StackFactory stackFactory;
    private final VpcFactory vpcFactory;
    private final TaggingService taggingService;

    private Construct scope;
    private Stack stack;
    private Environment env = Environment.DEV;
    private String namespace = "Default";
    private Map<String, String> tags = new HashMap<>();

    public void provision() {
        log.debug("provision");

        stack = stackFactory.create(scope == null ? root.getRootScope() : scope, namespace);

        Vpc vpc = addPublicPrivateIsolatedVpc();

        addTags();
    }

    private void addTags() {
        tags.put("Environment", env.name());
        taggingService.addTags(stack, tags, QUALIFIER.name());
        taggingService.addTags(stack, config.getPipeline().getTags(), QUALIFIER.name());
    }

    private Vpc addPublicPrivateIsolatedVpc() {
        log.debug("addPublicPrivateIsolatedVpc");

        // Perform any resource specific business logic here e.g. add nat gateway alarm

        return vpcFactory.create(stack, env);
    }

}
