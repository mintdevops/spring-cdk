package com.example.demo.stack;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.StackType;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.resource.ImageBuilderFactory;
import com.example.demo.resource.StackFactory;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageStackService implements IStack {

    private static final StackType QUALIFIER = StackType.IMAGE;

    private final Root root;
    private final AppConfig config;
    private final StackFactory stackFactory;
    private final ImageBuilderFactory imageBuilderFactory;
    private final TaggingService taggingService;
    private final LookupService lookupService;

    private Construct scope;
    private Stack stack;
    private Environment env = Environment.DEV;
    private String namespace = "Default";
    private Map<String, String> tags = new HashMap<>();

    public void provision() {
        log.debug("provision");

        stack = stackFactory.create(scope == null ? root.getRootScope() : scope, namespace);

        IImageBuilder builder = addImageBuilder();

        addTags();
    }

    private void addTags() {
        tags.put("Environment", env.name());
        taggingService.addTags(stack, tags, QUALIFIER.name());
        taggingService.addTags(stack, config.getPipeline().getTags(), QUALIFIER.name());
    }

    private IImageBuilder addImageBuilder() {
        log.debug("addImageBuilder");

        // Perform any resource specific business logic here

        return imageBuilderFactory.create(stack, env);
    }

}
