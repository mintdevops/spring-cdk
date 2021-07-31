package com.example.demo.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.IStack;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.repository.ImageBuilderFactory;

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

    private final Root root;
    private final AppConfig config;
    private final ImageBuilderFactory imageBuilderFactory;

    // We dont really know where the resources in this service would end up in the construct tree
    private Construct scope;
    private Stack stack;
    private Environment env = Environment.DEV;
    private String namespace = "Default";

    public void setScope(Construct scope) {
        this.scope = scope;
    }

    public void provision() {
        log.debug("provision");

        stack = Stack.Builder.create(scope == null ? root.getRootScope() : scope, namespace).build();

        IImageBuilder builder = addImageBuilder();
    }

    private IImageBuilder addImageBuilder() {
        log.debug("addImageBuilder");

        // Perform any resource specific business logic here

        return imageBuilderFactory.create(stack, env);
    }

}
