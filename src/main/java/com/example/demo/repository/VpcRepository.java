package com.example.demo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.CfnNag;
import com.example.demo.config.CfnNagRule;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.LookupType;
import com.example.demo.config.VpcConfig;
import com.example.demo.core.vpc.VpcEndpointServiceTypes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointOptions;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.NatProvider;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.amazon.awscdk.services.ssm.StringParameter;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class VpcRepository extends AbstractResourceRepository<IVpc, VpcConfig> {

    private final static String RESOURCE_NAME = "Vpc";

    private final AppConfig conf;

    @Override
    public Vpc create(Construct scope, String namespace, Environment stage, VpcConfig conf) {
        Vpc vpc = Vpc.Builder
                .create(scope, Label.builder()
                                    .namespace("")
                                    .stage("")
                                    .resource(RESOURCE_NAME)
                                    .build()
                                    .toString())
                .cidr(conf.getCidr())
                .maxAzs(Environment.PROD == stage ? 2 : 1)
                .natGateways(Environment.PROD == stage ? 2 : 1)
                .natGatewayProvider(conf.getNatProvider() != null ? conf.getNatProvider() : NatProvider.gateway())
                .subnetConfiguration(Arrays.asList(
                        SubnetConfiguration
                                .builder()
                                .name("public")
                                .cidrMask(Environment.DEV == stage ? 22 : 22)
                                .subnetType(SubnetType.PUBLIC)
                                .build(),
                        SubnetConfiguration
                                .builder()
                                .name("private")
                                .cidrMask(Environment.DEV == stage ? 22 : 20)
                                .subnetType(SubnetType.PRIVATE)
                                .build(),
                        SubnetConfiguration
                                .builder()
                                .name("isolated")
                                .cidrMask(Environment.DEV == stage ? 22 : 20)
                                .subnetType(SubnetType.ISOLATED)
                                .build()
                ))
                .build();

        addS3Endpoint(vpc);
        addVpcLogs(vpc);

        return vpc;
    }

    /**
     * Adds an S3 endpoint to the VPC.
     * <p>
     * When using a NAT gateway you need to manage egress traffic to control your AWS costs. Many applications access the
     * S3 service at both deploy and runtime so it usually a good idea to configure it as it can potentially save you
     * money if you're reading/writing large amounts of data from S3.
     */
    private void addS3Endpoint(Vpc vpc) {
        vpc.addGatewayEndpoint(
                VpcEndpointServiceTypes.S3.toString(),
                GatewayVpcEndpointOptions.builder().service(GatewayVpcEndpointAwsService.S3).build()
        );
    }

    /**
     * Adds flow logs to the VPC.
     * <p>
     * By default they are stored in S3 where they can be ingested in to other services for analysis e.g. GuardDuty
     * for threat detection or Athena for customized analysis.
     * <p>
     * Flow logs can be used to respond to security events, or analyse the performance of your applications at the
     * packet level.
     */
    private void addVpcLogs(Vpc vpc) {
        vpc.addFlowLog("FlowLog");

        // These will nearly always be publicly accessible instances with either an AWS assigned IP or a customer
        // assigned EIP
        // If using cfg_nag (https://github.com/stelligent/cfn_nag) don't flag these instances
        List<ISubnet> subnets = vpc.getPublicSubnets();
        for (ISubnet s : subnets) {
            CfnSubnet cfnSubnet = (CfnSubnet) s.getNode().getDefaultChild();

            CfnNag nag = CfnNag.builder().rules_to_suppress(Collections.singletonList(
                    CfnNagRule.builder()
                              .id("W33")
                              .reason("Allow Public Subnets to have MapPublicIpOnLaunch set to true")
                              .build()
            )).build();

            Map<String, Object> metadata = new HashMap<>();

            metadata.put("cfn_nag", nag);

            cfnSubnet.getCfnOptions().setMetadata(metadata);
        }

    }

    @Override
    public IVpc lookup(Construct scope, String stackName, LookupType lookupType) {
        // TODO: Strategy pattern to pick between LookupType.SYNTH and LookupType.DEPLOY
        return lookupVpcAtDeployByStackName(scope, stackName);
    }

    private IVpc lookupVpcAtDeployByStackName(Construct scope, String stackName) {
        if (conf.isDemo()) {
            return Vpc.fromVpcAttributes(scope, "DemoVpc",
                    VpcAttributes.builder()
                                 .vpcId("vpc-12345")
                                 .publicSubnetIds(Collections.singletonList("subnet-123"))
                                 .privateSubnetIds(Collections.singletonList("subnet-456"))
                                 .isolatedSubnetIds(Collections.singletonList("subnet-789"))
                                 .availabilityZones(Collections.singletonList("eu-west-1a"))
                                 .build());
        }

        String vpcId = StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSsmParam(stackName, "VpcId")
                );
        List<String> azs = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSsmParam(stackName, "VpcAZs")
                ).split(","));
        List<String> publicSubnets = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSsmParam(stackName, "VpcPublicSubnets")
                ).split(","));
        List<String> privateSubnets = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSsmParam(stackName, "VpcPrivateSubnets")
                ).split(","));
        List<String> isolatedSubnets = Arrays.asList(StringParameter
                .valueForStringParameter(
                        scope,
                        stackNameToSsmParam(stackName, "VpcIsolatedSubnets")
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
