package com.example.demo.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Externalised configuration for pipeline source configuration.
 *
 * NB: Even though its called GitHub it supports all git based repository sources. See
 * <a href="https://docs.aws.amazon.com/codepipeline/latest/userguide/action-reference-CodestarConnectionSource.html">here</a>
 * for more information.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.pipeline.github")
@Validated
public class GithubConfig {

    /**
     * The Owner.
     */
    @NotEmpty
    String owner;
    /**
     * The Repo.
     */
    @NotEmpty
    String repo;
    /**
     * The Branch.
     */
    String branch = "main";
    /**
     * The name of the AWS Secrets Manager secret containing a token with sufficient scope to clone the repository.
     */
    String token = "GITHUB_TOKEN";

}
