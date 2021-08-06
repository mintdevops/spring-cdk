package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.config.StackType;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.core.pipeline.StackFactory;
import com.example.demo.repository.ImageBuilderRepository;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageStackService extends AbstractStackService {

    public static final StackType QUALIFIER = StackType.IMAGE;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final ImageBuilderRepository imageBuilderRepository;

    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, namespace);

        addImageBuilder(stack, stage, config.getEnv().get(stage).getImage());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    private IImageBuilder addImageBuilder(Stack stack, Environment stage, ImageBuildConfig imageBuilderConf) {
        log.debug("addImageBuilder");

        // Perform any resource specific business logic here

        return imageBuilderRepository.create(stack, "", stage, imageBuilderConf);
    }

}
