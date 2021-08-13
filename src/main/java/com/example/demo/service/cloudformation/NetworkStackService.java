package com.example.demo.service.cloudformation;

import java.util.ArrayList;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.StackType;
import com.example.demo.config.VpcConfig;
import com.example.demo.construct.natgateway.NatGatewayConfig;
import com.example.demo.construct.vpc.VpcSpec;
import com.example.demo.repository.NatGatewayRepository;
import com.example.demo.repository.VpcRepository;
import com.example.demo.core.StackFactory;
import com.example.demo.service.AbstractInfrastructureService;
import com.example.demo.service.TaggingService;

import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.NatProvider;
import software.amazon.awscdk.services.ec2.Vpc;


/**
 * A service to provision a Cloudformation stack for common network resources.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NetworkStackService extends AbstractInfrastructureService {

    /**
     * The constant QUALIFIER.
     */
    public static final StackType QUALIFIER = StackType.NETWORK;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final StackOutputService stackOutputService;
    private final VpcRepository vpcRepository;
    private final NatGatewayRepository natGatewayRepository;

    public Stack provision(Construct scope, String namespace, Environment stage) {
        Stack stack = stackFactory.create(scope, namespace, stage);

        addPublicPrivateIsolatedVpc(stack, stage, config.getEnv().get(stage).getVpc());

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    /**
     * Add a public/private/isolated VPC to the stack.
     *
     * @param stack
     * @param stage
     * @param vpcConf
     */
    private void addPublicPrivateIsolatedVpc(Stack stack, Environment stage, VpcConfig vpcConf) {
        // Perform any stack specific domain logic here

        log.info("Creating NAT gateway");

        NatProvider nat = natGatewayRepository.create(stack, "", stage,
                NatGatewayConfig.builder()
                                .egressThreshold(vpcConf.getNat().getEgressThreshold())
                                .allocationIds(new ArrayList<>())
                                .build()).getNatProvider();

        log.info("Creating VPC");

        Vpc vpc = vpcRepository.create(stack, "", stage, VpcSpec.builder()
                                                                .cidr(vpcConf.getCidr())
                                                                .natProvider(nat)
                                                                .publicSubnetCidrMask(Environment.DEV == stage ?
                                                                        22 : 22)
                                                                .privateSubnetCidrMask(Environment.DEV == stage ?
                                                                        22 : 20)
                                                                .isolatedSubnetCidrMask(Environment.DEV == stage ?
                                                                        22 : 20)
                                                                .build());

        vpcRepository.export(stack, vpc)
                     .forEach(cfnOutput -> stackOutputService.addOutput(stack, stage, cfnOutput));
    }

}
