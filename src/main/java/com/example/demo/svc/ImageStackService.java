package com.example.demo.svc;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.config.Label;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;
import com.example.demo.repository.ImageBuilderFactory;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

@Component
@Log4j2
@Setter
public class ImageStackService implements IStack {

    private final static String RESOURCE_NAME = "Image";

    @Autowired
    Root root;

    @Autowired
    AppConfig config;

    @Autowired
    ImageBuilderFactory imageBuilderFactory;

    Construct scope;
    Stack stack;
    Environment env = Environment.DEV;
    String namespace = "Default";

    public void setScope(Construct scope) {
        this.scope = scope;
    }

    public void provision() {
        log.debug("provision");

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope, namespace)
//                             .stackName(
//                                     Label.builder()
//                                          .namespace(namespace)
//                                          .stage("")
//                                          .resource("")
//                                          .build()
//                                          .toString()
//                             )
                             .build();

        IImageBuilder builder = addImageBuilder();
    }

    private IImageBuilder addImageBuilder() {
        log.debug("addImageBuilder");

        // Perform any resource specific business logic here

        return imageBuilderFactory.create(stack, env);
    }

}
