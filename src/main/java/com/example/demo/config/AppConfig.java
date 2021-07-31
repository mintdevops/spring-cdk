package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {

    VpcConfig vpc = new VpcConfig();

    // Getters and Setters (Omitted for brevity)

}