package com.example.demo.svc;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;
import com.example.demo.repository.ImageBuilderFactory;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

@Component
@Log4j2
public class ImageStackService implements IStack {

    @Autowired
    Root root;

    @Autowired
    AppConfig config;

    @Autowired
    ImageBuilderFactory imageBuilderFactory;

    Construct scope;
    Stack stack;
    Environment env = Environment.DEV;

    public void setScope(Construct scope) {
        this.scope = scope;
    }

    public void provision() {
        log.debug("ImageStackService:provision");
        log.debug(config);

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope).build();

        IImageBuilder builder = addImageBuilder();
    }

    private IImageBuilder addImageBuilder() {
        // Perform any resource specific business logic here e.g. add nat gateway alarm

        // Example of a custom resource (which is still a construct but its our own)
        return imageBuilderFactory.create(stack, Environment.DEV);
    }

    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
    }

}
