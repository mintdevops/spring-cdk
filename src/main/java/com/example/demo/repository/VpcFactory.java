package com.example.demo.repository;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.VpcConfig;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

@Component
@Log4j2
public class VpcFactory {

    private final static String RESOURCE_NAME = "Vpc";

    public Vpc create(Construct parent, VpcConfig conf, Environment stage) {
        log.debug("VpcFactory:create");
        log.debug(stage);
        log.debug(conf);

        return Vpc.Builder
                .create(parent, RESOURCE_NAME)
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

    // standard setters and getters
}