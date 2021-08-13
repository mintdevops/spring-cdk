package com.example.demo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.core.Environment;
import com.example.demo.core.StackType;
import com.example.demo.core.ValueOfEnum;

import lombok.Data;

/**
 * Externalised configuration for the Codepipeline pipeline.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.pipeline")
@Validated
public class PipelineConfig {

    /**
     * Name of the pipeline in CodePipeline console
     */
    private String name = "MintDevops";

    /**
     * The GitHub (source) repository configuration.
     */
    private GithubConfig github = new GithubConfig();

    /**
     * The pipeline deployment configuration.
     */
    private DeployConfig deploy = new DeployConfig();

    /**
     * The stack type the pipeline is managing (controls pipeline scaffolding).
     */
    @ValueOfEnum(enumClass = StackType.class)
    private String stack;

    /**
     * A list of environments to create. Promotion occurs from left to right e.g. dev->test->prod
     * <p>
     * The underlying stack will be deployed to each environment.
     */
    @Size(min = 1)
    private List<Environment> environments = new ArrayList<>();

    /**
     * A tag set to apply to all pipeline resources. Uses the `pipeline` qualifier.
     */
    private Map<String, String> tags = new HashMap<>();

}
