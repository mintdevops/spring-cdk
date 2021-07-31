package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.vpc")
@Validated
public class VpcConfig implements IStackConfig {

    private String cidr = "172.0.0.1/16";

}
