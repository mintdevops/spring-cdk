package com.example.demo.service.cloudformation;

import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.AsgConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;
import com.example.demo.core.StackType;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.immutableserver.ImmutableServer;
import com.example.demo.construct.immutableserver.ImmutableServerSpec;
import com.example.demo.core.StackFactory;
import com.example.demo.repository.ImageBuilderRepository;
import com.example.demo.repository.ImmutableServerRepository;
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
 * A service to provision a Cloudformation stack to manage an application deployed to EC2.
 */
@Component
@Log4j2
@Setter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AppStackService extends AbstractInfrastructureService {

    /**
     * The constant QUALIFIER.
     */
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
        Stack stack = stackFactory.create(scope, namespace, stage);

        addEc2Workload(stack, stage, config.getEnv().get(stage).getAsg());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    /**
     * Adds an EC2 workload to the stack. To ensure immutability we first need to create a development environment which
     * can deploy the latest version of the application code. Once successfully deployed (and tested) an
     * application-specific image will be taken and it will be promoted through other environments ensuring the the
     * underlying infrastructure and deployment configuration cannot change.
     *
     * @param stack
     * @param stage
     * @param conf
     */
    private void addEc2Workload(Stack stack, Environment stage, AsgConfig conf){
        IVpc vpc = vpcRepository.lookup(stack, conf.getVpcStackName(), LookupType.DEPLOY);

        IImageBuilder image;
        if (stage == Environment.DEV) {
            log.info("Injecting golden image");

            image = imageBuilderRepository.lookup(stack, conf.getImageStackName(), LookupType.SYNTH);
        }
        else {
            log.debug("Injecting application image");

            image = imageBuilderRepository.lookup(
                    stack,
                    coalesceStackName(stack, Environment.DEV, stage),
                    LookupType.DEPLOY
            );
        }

        log.info("Creating immutable server");

        ImmutableServer server = immutableServerRepository.create(stack, "", stage,
                ImmutableServerSpec.builder()
                                   .imageRegion(config.getEnv().get(stage).getDeploy().getRegion())
                                   .vpc(vpc)
                                   .imageId(image.getAmiId())
                                   .asg(conf)
                                   .build());


        immutableServerRepository.export(stack, server)
                                 .forEach(cfnOutput -> stackOutputService.addOutput(stack,  stage, cfnOutput));
    }

    /**
     * Convert stack names between environment stages
     * @param stack
     * @param stageFrom
     * @param stageTo
     * @return
     */
    private String coalesceStackName(Stack stack, Environment stageFrom, Environment stageTo) {
        return Stack.of(stack)
                    .getStackName()
                    .replaceAll(WordUtils.capitalizeFully(stageTo.name()), WordUtils.capitalizeFully(stageFrom.name()));
    }

}
