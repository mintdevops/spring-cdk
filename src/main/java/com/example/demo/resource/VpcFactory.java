package com.example.demo.resource;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.VpcConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class VpcFactory {

    private final static String RESOURCE_NAME = "Vpc";

    private final AppConfig conf;

    public Vpc create(Construct parent, Environment stage) {
        log.debug("create");

        VpcConfig vpcConf = conf.getEnv().get(stage).getVpc();

        return Vpc.Builder
                .create(parent, Label.builder()
                                     .namespace("")
                                     .stage("")
                                     .resource(RESOURCE_NAME)
                                     .build()
                                     .toString())
                .cidr(vpcConf.getCidr())
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

}