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
    PipelineConfig pipeline = new PipelineConfig();
    Map<Environment, EnvConfig> env = new HashMap<>();
}