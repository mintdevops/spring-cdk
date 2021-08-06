package com.example.demo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.repository.IResourceConfig;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.pipeline")
@Validated
public class PipelineConfig implements IResourceConfig {

    private String name = "MyAwesomeCDKPipeline";
    private GithubConfig github = new GithubConfig();
    private DeployConfig deploy = new DeployConfig();
    private StackType stack;
    private List<Environment> environments = new ArrayList<>();
    private Map<String, String> tags = new HashMap<>();

}
