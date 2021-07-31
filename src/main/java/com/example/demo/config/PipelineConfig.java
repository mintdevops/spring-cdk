package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.pipeline")
@Validated
public class PipelineConfig {

    private String name = "MyAwesomeCDKPipeline";
    private GithubConfig github = new GithubConfig();
    private StackType stack;

}
