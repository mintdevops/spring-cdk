package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.construct.ISpec;

import lombok.Data;

@Data
@Component
@Validated
public class VpcConfig implements ISpec {

    private String cidr = "172.0.0.1/16";
    private NatGatewayConfig nat = new NatGatewayConfig();

}
