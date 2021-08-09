package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.config.LookupType;
import com.example.demo.config.StackType;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;
import com.example.demo.core.pipeline.StackFactory;
import com.example.demo.repository.ImageBuilderRepository;
import com.example.demo.repository.VpcRepository;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.IVpc;

@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageStackService extends AbstractStackService {

    public static final StackType QUALIFIER = StackType.IMAGE;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final VpcRepository vpcRepository;
    private final ImageBuilderRepository imageBuilderRepository;

    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, namespace);

        addImageBuilder(stack, stage, config.getEnv().get(stage).getImage());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    private void addImageBuilder(Stack stack, Environment stage, ImageBuildConfig conf) {
        log.debug("addImageBuilder");

        // Perform any stack specific domain logic here

        IVpc vpc = vpcRepository.lookup(stack, conf.getVpcStackName(), LookupType.DEPLOY);

        IImageBuilder builder = imageBuilderRepository.create(stack, "", stage, ImageBuilderConfig.builder()
                                                                                 .vpc(vpc)
                                                                                 .imageName(conf.getImageName())
                                                                                 .accounts(conf.getAccounts())
                                                                                 .regions(conf.getRegions())
                                                                                 .build());

        imageBuilderRepository.exportSSM(stack, imageBuilderRepository.export(stack, builder));
    }

}
