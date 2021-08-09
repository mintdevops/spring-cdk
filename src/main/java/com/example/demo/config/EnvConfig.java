package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Component
@Validated
public class EnvConfig {

    VpcConfig vpc = new VpcConfig();
    ImageBuildConfig image = new ImageBuildConfig();
    DeployConfig deploy = new DeployConfig();
    Map<String, String> tags = new HashMap<>();
    AsgConfig asg = new AsgConfig();

}