package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * The type Deploy config.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.pipeline.deploy")
@Validated
public class DeployConfig {

    String account = "";
    String region = "eu-west-1";

}
