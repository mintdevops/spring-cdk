package com.example.demo.service;

import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.AsgConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.LookupType;
import com.example.demo.config.StackType;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.immutableserver.ImmutableServer;
import com.example.demo.construct.immutableserver.ImmutableServerConfig;
import com.example.demo.core.pipeline.StackFactory;
import com.example.demo.repository.ImageBuilderRepository;
import com.example.demo.repository.ImmutableServerRepository;
import com.example.demo.repository.VpcRepository;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.IVpc;

@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AppStackService extends AbstractStackService {

    public static final StackType QUALIFIER = StackType.WORKLOAD;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final VpcRepository vpcRepository;
    private final ImageBuilderRepository imageBuilderRepository;
    private final ImmutableServerRepository immutableServerRepository;
    private final StackOutputService stackOutputService;

    @Override
    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, namespace);

        addEc2Workload(stack, stage, config.getEnv().get(stage).getAsg());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    private void addEc2Workload(Stack stack, Environment stage, AsgConfig conf){
        IVpc vpc = vpcRepository.lookup(stack, conf.getVpcStackName(), LookupType.DEPLOY);

        IImageBuilder image;

        if (stage == Environment.DEV) {
            log.debug("Using golden image");

            image = imageBuilderRepository.lookup(stack, conf.getImageStackName(), LookupType.SYNTH);
        }
        else {
            log.debug("Using application image");

            image = imageBuilderRepository.lookup(stack, coalesceStackName(stack, Environment.DEV
                    , stage),
                    LookupType.DEPLOY);
        }

        ImmutableServer server = immutableServerRepository.create(stack, "", stage,
                ImmutableServerConfig.builder().vpc(vpc).imageId(image.getAmiId()).asg(conf).build());


        immutableServerRepository.export(stack, server).forEach(cfnOutput -> stackOutputService.addOutput(stack,  stage,
                cfnOutput));
    }

    private String coalesceStackName(Stack stack, Environment stageFrom, Environment stageTo) {
        return Stack.of(stack).getStackName().replaceAll(WordUtils.capitalizeFully(stageTo.name()),
                WordUtils.capitalizeFully(stageFrom.name()));
    }
}
