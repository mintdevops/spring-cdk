package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;

import lombok.Data;
import software.amazon.awscdk.core.Construct;

/**
 * The CDK application config.
 *
 * Leverages Spring Boot to bind properties or YAML file to POJO and supports JSR 349 validation with Hibernate.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {

    /**
     * The git commit.
     */
    String version;
    /**
     * The application name. This will be used to generate resource names throughout
     */
    String applicationName;
    /**
     * The tag namespace. This is to customize the tag key prefix applied to resources.
     * The tag format is [<namespace>.]<qualifier>/<tag-key>
     */
    String tagNamespace;
    /**
     * The Pipeline configuration.
     */
    PipelineConfig pipeline = new PipelineConfig();
    /**
     * The Environment configuration.
     */
    Map<Environment, EnvConfig> env = new HashMap<>();
    /**
     * A set of tags to apply to all resources in the application. Uses the `app` qualifier.
     */
    Map<String, String> tags = new HashMap<>();
    /**
     * Demo flag.
     *
     * Return fake resources when calling
     * {@link com.example.demo.repository.IResourceRepository#lookup(Construct, String, LookupType)} to allow `cdk
     * synth` to be run without AWS credentials even if the application has resource dependencies defined in another
     * application.
     */
    boolean demo = false;

    /**
     * Instantiates a new App config.
     */
    public AppConfig() {
        EnvConfig dev = new EnvConfig();
        EnvConfig test = new EnvConfig();
        EnvConfig perf = new EnvConfig();
        EnvConfig prod = new EnvConfig();

        env.put(Environment.DEV, dev);
        env.put(Environment.TEST, test);
        env.put(Environment.PERF, perf);
        env.put(Environment.PROD, prod);
    }
}