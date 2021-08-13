package com.example.demo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;
import com.example.demo.construct.vpc.CustomVpc;
import com.example.demo.construct.vpc.VpcSpec;
import com.example.demo.core.Label;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.amazon.awscdk.services.ssm.StringParameter;

/**
 * See {@link IResourceRepository} for more information.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class VpcRepository extends AbstractResourceRepository<IVpc, VpcSpec> {

    private final static String RESOURCE_NAME = "Vpc";

    private final AppConfig conf;

    @Override
    public Vpc create(Construct scope, String namespace, Environment stage, VpcSpec spec) {
        return new CustomVpc(scope, Label.builder()
                                         .namespace(namespace)
                                         .stage(stage)
                                         .resource(RESOURCE_NAME)
                                         .build()
                                         .toLogicalId(), spec).getVpc();
    }

    @Override
    public IVpc lookup(Construct scope, String stackName, LookupType lookupType) {
        if (conf.isDemo()) {
            log.debug("Creating demo VPC");

            return Vpc.fromVpcAttributes(scope, "DemoVpc",
                    VpcAttributes.builder()
                                 .vpcId("vpc-12345")
                                 .availabilityZones(Collections.singletonList("eu-west-1a"))
                                 .publicSubnetIds(Collections.singletonList("subnet-123"))
                                 .privateSubnetIds(Collections.singletonList("subnet-456"))
                                 .isolatedSubnetIds(Collections.singletonList("subnet-789"))
                                 .build());
        }

        String vpcId = StringParameter.valueForStringParameter(
                scope,
                Label.builder()
                     .namespace(stackName)
                     .resource("VpcId")
                     .build()
                     .toSsmParameterName()
        );

        List<String> azs = Arrays.asList(StringParameter.valueForStringParameter(
                scope,
                Label.builder()
                     .namespace(stackName)
                     .resource("VpcAZs")
                     .build()
                     .toSsmParameterName()
        ).split(","));

        List<String> publicSubnets = Arrays.asList(StringParameter.valueForStringParameter(
                scope,
                Label.builder()
                     .namespace(stackName)
                     .resource("VpcPublicSubnets")
                     .build()
                     .toSsmParameterName()
        ).split(","));

        List<String> privateSubnets = Arrays.asList(StringParameter.valueForStringParameter(
                scope,
                Label.builder()
                     .namespace(stackName)
                     .resource("VpcPrivateSubnets")
                     .build()
                     .toSsmParameterName()
        ).split(","));

        List<String> isolatedSubnets = Arrays.asList(StringParameter.valueForStringParameter(
                scope,
                Label.builder()
                     .namespace(stackName)
                     .resource("VpcIsolatedSubnets")
                     .build()
                     .toSsmParameterName()
        ).split(","));

        log.debug("Looking up vpc at {} time {} {} {} {} {}", lookupType.name(), vpcId, azs, publicSubnets,
                privateSubnets,
                isolatedSubnets);

        return Vpc.fromVpcAttributes(scope, stackName,
                VpcAttributes
                        .builder()
                        .vpcId(vpcId)
                        .availabilityZones(azs)
                        .privateSubnetIds(privateSubnets)
                        .publicSubnetIds(publicSubnets)
                        .isolatedSubnetIds(isolatedSubnets)
                        .build());
    }

    public List<CfnOutput> export(Construct scope, IVpc resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "VpcId", "The Vpc Id", resource.getVpcId()));
        outputs.add(createOutput(scope, "VpcAZs", "The VPC availability zones", String.join(",",
                resource.getAvailabilityZones())));
        outputs.add(createOutput(scope, "VpcPublicSubnets", "The VPC Public Subnets", resource
                .getPrivateSubnets()
                .stream()
                .map(ISubnet::getSubnetId)
                .collect(Collectors
                        .joining(","))));
        outputs.add(createOutput(scope, "VpcPrivateSubnets", "The VPC Private Subnets",
                resource.getPublicSubnets().stream().map(ISubnet::getSubnetId).collect(Collectors.joining(","))));
        outputs.add(createOutput(scope, "VpcIsolatedSubnets", "The VPC Isolated Subnets",
                resource.getIsolatedSubnets().stream().map(ISubnet::getSubnetId).collect(Collectors.joining(","))));

        return outputs;
    }
}
