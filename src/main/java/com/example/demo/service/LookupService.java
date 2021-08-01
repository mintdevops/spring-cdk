package com.example.demo.service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.app.Root;
import com.example.demo.resource.StackFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.ssm.StringParameter;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class LookupService {

    private final Root root;
    private final StackFactory stackFactory;
    private Stack stack;

    @PostConstruct
    public void provision() {
        stack = stackFactory.create(root.getRootScope(), "Lookup");
    }

    // TODO: Two separate beans with different lookup strategies: synth and deploy
    public IVpc lookupVpcAtSynthByStackName(Construct scope, String stackName) {
        String vpcId = StringParameter
                .valueFromLookup(
                        scope,
                        stackNameToSSMParam(stackName, "VpcId")
                );
        List<String> azs = Arrays.asList(StringParameter
                .valueFromLookup(
                        scope,
                        stackNameToSSMParam(stackName, "VpcAZs")
                ).split(","));
        List<String> publicSubnets = Arrays.asList(StringParameter
                .valueFromLookup(
                        scope,
                        stackNameToSSMParam(stackName, "VpcPublicSubnets")
                ).split(","));
        List<String> privateSubnets = Arrays.asList(StringParameter
                .valueFromLookup(
                        scope,
                        stackNameToSSMParam(stackName, "VpcPrivateSubnets")
                ).split(","));
        List<String> isolatedSubnets = Arrays.asList(StringParameter
                .valueFromLookup(
                        scope,
                        stackNameToSSMParam(stackName, "VpcIsolatedSubnets")
                ).split(","));

        log.debug("Looking up vpc at synth time {}", vpcId);

        return Vpc.fromLookup(scope, stackName,
                VpcLookupOptions.builder()
                                .vpcId(vpcId)
                                .build()
        );
    }

    public IVpc lookupVpcAtDeployByStackName(Construct scope, String stackName) {
        String vpcId = StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSSMParam(stackName, "VpcId")
                );
        List<String> azs = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSSMParam(stackName, "VpcAZs")
                ).split(","));
        List<String> publicSubnets = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSSMParam(stackName, "VpcPublicSubnets")
                ).split(","));
        List<String> privateSubnets = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSSMParam(stackName, "VpcPrivateSubnets")
                ).split(","));
        List<String> isolatedSubnets = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSSMParam(stackName, "VpcIsolatedSubnets")
                ).split(","));

        log.debug("Looking up vpc at deploy time {}", vpcId);

        return Vpc.fromVpcAttributes(scope, stackName,
                VpcAttributes
                        .builder()
                        .vpcId(vpcId)
                        .privateSubnetIds(privateSubnets)
                        .publicSubnetIds(publicSubnets)
                        .isolatedSubnetIds(isolatedSubnets)
                        .availabilityZones(azs)
                        .build());
    }

    private String stackNameToSSMParam(String stackName, String param) {
        return String.format("/%s/%s", stackName, param);
    }

}
