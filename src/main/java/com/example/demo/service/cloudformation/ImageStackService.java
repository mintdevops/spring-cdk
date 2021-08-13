package com.example.demo.service.cloudformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.core.LookupType;
import com.example.demo.core.StackType;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderSpec;
import com.example.demo.core.StackFactory;
import com.example.demo.repository.ImageBuilderRepository;
import com.example.demo.repository.VpcRepository;
import com.example.demo.service.AbstractInfrastructureService;
import com.example.demo.service.TaggingService;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.IVpc;


/**
 * A service to provision a Cloudformation stack producing a machine image.
 */
@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageStackService extends AbstractInfrastructureService {

    /**
     * The constant QUALIFIER.
     */
    public static final StackType QUALIFIER = StackType.IMAGE;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final VpcRepository vpcRepository;
    private final ImageBuilderRepository imageBuilderRepository;
    private final StackOutputService stackOutputService;

    public Stack provision(Construct scope, String namespace, Environment stage) {
        Stack stack = stackFactory.create(scope, namespace, stage);

        addImageBuilder(stack, stage, config.getEnv().get(stage).getImage());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    /**
     * Adds an image pipeline to the stack.
     *
     * @param stack
     * @param stage
     * @param conf
     */
    private void addImageBuilder(Stack stack, Environment stage, ImageBuildConfig conf) {
        // Perform any stack specific domain logic here

        IVpc vpc = vpcRepository.lookup(stack, conf.getVpcStackName(), LookupType.DEPLOY);

        log.info("Creating image pipeline");

        IImageBuilder builder = imageBuilderRepository.create(stack, "", stage, ImageBuilderSpec.builder()
                                                                                                .vpc(vpc)
                                                                                                .imageName(conf.getImageName())
                                                                                                .accounts(conf.getAccounts())
                                                                                                .regions(conf.getRegions())
                                                                                                .build());

        imageBuilderRepository.export(stack, builder)
                              .forEach(cfnOutput -> stackOutputService.addOutput(stack,  stage, cfnOutput));
    }

}
