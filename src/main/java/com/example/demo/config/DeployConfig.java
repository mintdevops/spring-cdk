package com.example.demo.config;

import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Externalised configuration for the deployment configuration of the pipeline stack.
 *
 * These parameters are required if you want to deploy the stacks managed by the pipeline to other accounts/regions
 * (primarily due to cross account permissions).
 *
 * Failure to provide these values will result in CDK using CDK_DEFAULT_ACCOUNT and CDK_DEFAULT_REGION env vars.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.pipeline.deploy")
@Validated
public class DeployConfig {

    /**
     * The Account.
     */
    @Pattern(regexp = "(^$|\\d{12})")
    String account = "";
    /**
     * The Region.
     */
    // TODO: Validate list of available regions
    String region = "";

}
