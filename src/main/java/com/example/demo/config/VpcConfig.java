package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.repository.IResourceConfig;

import lombok.Data;
import software.amazon.awscdk.services.ec2.NatProvider;

@Data
@Component
@ConfigurationProperties(prefix = "app.vpc")
@Validated
public class VpcConfig implements IResourceConfig {

    private String cidr = "172.0.0.1/16";
    private NatGatewayConfig nat = new NatGatewayConfig();
    private NatProvider natProvider;

}
