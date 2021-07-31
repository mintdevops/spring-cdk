package com.example.demo.svc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.Label;
import com.example.demo.config.TagManager;
import com.example.demo.repository.VpcFactory;
import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Tags;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Setter
public class NetworkStackService implements IStack {

    private final Root root;
    private final AppConfig config;
    private final VpcFactory vpcFactory;
    private final TaggingService taggingService;

    private Construct scope;
    private Stack stack;
    private Environment env = Environment.DEV;
    private String namespace = "Default";
    private Map<String, String> tags = new HashMap<>();

    public void provision() {
        log.debug("provision");

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope, namespace).build();

        Vpc vpc = addPublicPrivateIsolatedVpc();

        taggingService.addStackTags(stack);
    }

    // @PostConstruct
    // private void addTags() {
    //     Map<String, String> merged = config.getEnv().get(env).getTags();
    //     merged.put("Environment", env.toString());

    //     tags = TagManager.fullyQualifiedTags(config.getTagNamespace(), "image",
    //             merged);
    // }

    private Vpc addPublicPrivateIsolatedVpc() {
        log.debug("addPublicPrivateIsolatedVpc");

        // Perform any resource specific business logic here e.g. add nat gateway alarm

        return vpcFactory.create(stack, env);
    }

}
