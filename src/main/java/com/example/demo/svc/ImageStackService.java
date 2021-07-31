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
import com.example.demo.config.TagManager;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.repository.ImageBuilderFactory;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.Tags;

@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageStackService implements IStack {

    private final Root root;
    private final AppConfig config;
    private final ImageBuilderFactory imageBuilderFactory;
    private final TaggingService taggingService;

    private Construct scope;
    private Stack stack;
    private Environment env = Environment.DEV;
    private String namespace = "Default";
    private Map<String, String> tags = new HashMap<>();

    public void provision() {
        log.debug("provision");

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope, namespace).build();

        IImageBuilder builder = addImageBuilder();

        // for (Map.Entry<String, String> entry : tags.entrySet()) {
        //     Tags.of(stack).add(entry.getKey(), entry.getValue());
        // }

        taggingService.addStackTags(stack);
    }

    // @PostConstruct
    // private void addTags() {
    //     Map<String, String> merged = config.getEnv().get(env).getTags();
    //     merged.put("Environment", env.toString())   ;

    //     tags = TagManager.fullyQualifiedTags(config.getTagNamespace(), "image",
    //             merged);
    // }

    private IImageBuilder addImageBuilder() {
        log.debug("addImageBuilder");

        // Perform any resource specific business logic here

        return imageBuilderFactory.create(stack, env);
    }

}
