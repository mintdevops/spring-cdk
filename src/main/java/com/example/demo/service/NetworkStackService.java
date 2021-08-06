package com.example.demo.service;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.StackType;
import com.example.demo.config.VpcConfig;
import com.example.demo.construct.nat.NatGatewayConfig;
import com.example.demo.repository.NatGatewayRepository;
import com.example.demo.repository.VpcRepository;
import com.example.demo.core.pipeline.StackFactory;

import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.NatProvider;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Setter
public class NetworkStackService extends AbstractStackService {

    public static final StackType QUALIFIER = StackType.NETWORK;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final VpcRepository vpcRepository;
    private final NatGatewayRepository natGatewayRepository;
    private final PipelineStageService pipelineStageService;

    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, namespace);

        addPublicPrivateIsolatedVpc(stack, stage, config.getEnv().get(stage).getVpc());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    private void addPublicPrivateIsolatedVpc(Stack stack, Environment stage, VpcConfig vpcConf) {
        log.debug("addPublicPrivateIsolatedVpc");

        // Perform any stack specific domain logic here

        NatProvider nat = natGatewayRepository.create(stack, "", stage, NatGatewayConfig.builder().build()).getNatProvider();

        vpcConf.setNat(nat);

        Vpc vpc = vpcRepository.create(stack, "", stage, vpcConf);

        vpcRepository.export(stack, vpc).stream().forEach(pipelineStageService::addOutput);
    }

}
