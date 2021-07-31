package com.example.demo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Environment> environments = new ArrayList<>();
    private Map<String, String> tags = new HashMap<>();

}
