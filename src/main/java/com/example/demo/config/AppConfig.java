package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {

    String name;
    String tagNamespace;
    PipelineConfig pipeline = new PipelineConfig();
    Map<Environment, EnvConfig> env = new HashMap<>();
    Map<String, String> tags = new HashMap<>();
    boolean demo = false;

    public AppConfig() {
        env.put(Environment.CICD, new EnvConfig());
        env.put(Environment.BUILD, new EnvConfig());
        env.put(Environment.DEMO, new EnvConfig());
        env.put(Environment.DEV, new EnvConfig());
        env.put(Environment.TEST, new EnvConfig());
        env.put(Environment.PERF, new EnvConfig());
        env.put(Environment.PROD, new EnvConfig());
    }
}