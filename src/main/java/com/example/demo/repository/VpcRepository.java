package com.example.demo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.VpcConfig;
import com.example.demo.service.PipelineStageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.IVpc;
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

    @Override
    public Vpc create(Construct scope, String namespace, Environment stage, VpcConfig conf) {
        return Vpc.Builder
                .create(scope, Label.builder()
                                     .namespace("")
                                     .stage("")
                                     .resource(RESOURCE_NAME)
                                     .build()
                                     .toString())
                .cidr(conf.getCidr())
                .maxAzs(Environment.PROD == stage ? 2 : 1)
                .natGateways(Environment.PROD == stage ? 2 : 1)
                //.natGatewayProvider(nat)
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
    }

    @Override
    public IVpc lookup(Construct scope, String stackName, LookupType lookupType) {
        // TODO: Strategy pattern to pick between LookupType.SYNTH and LookupType.DEPLOY
        return lookupVpcAtDeployByStackName(scope, stackName);
    }

    private IVpc lookupVpcAtDeployByStackName(Construct scope, String stackName) {
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

    public List<CfnOutput> export(Construct scope, IVpc resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "VpcId", "The Vpc Id", resource.getVpcId()));

        return outputs;
    }
}
