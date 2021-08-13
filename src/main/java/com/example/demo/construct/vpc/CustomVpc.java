package com.example.demo.construct.vpc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointOptions;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.NatProvider;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

@Getter
public class CustomVpc extends Construct {

    private final Vpc vpc;

    /**
     * Instantiates a new Custom vpc.
     *
     * @param scope the scope
     * @param id    the id
     * @param spec  the spec
     */
    public CustomVpc(software.constructs.@NotNull Construct scope, @NotNull String id, VpcSpec spec) {
        super(scope, id);

        System.out.println(spec);
        vpc = Vpc.Builder
                .create(scope, "Vpc")
                .cidr(spec.getCidr())
                .maxAzs(spec.getMaxAzs())
                .natGateways(spec.getMaxAzs())
                .natGatewayProvider(spec.getNatProvider() != null ? spec.getNatProvider() : NatProvider.gateway())
                .subnetConfiguration(Arrays.asList(
                        SubnetConfiguration
                                .builder()
                                .name("public")
                                .cidrMask(spec.getPublicSubnetCidrMask())
                                .subnetType(SubnetType.PUBLIC)
                                .build(),
                        SubnetConfiguration
                                .builder()
                                .name("private")
                                .cidrMask(spec.getPrivateSubnetCidrMask())
                                .subnetType(SubnetType.PRIVATE)
                                .build(),
                        SubnetConfiguration
                                .builder()
                                .name("isolated")
                                .cidrMask(spec.getIsolatedSubnetCidrMask())
                                .subnetType(SubnetType.ISOLATED)
                                .build()
                ))
                .build();

        addS3Endpoint(vpc);
        addVpcLogs(vpc);
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

}
