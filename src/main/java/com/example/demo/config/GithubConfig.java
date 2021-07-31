package com.example.demo.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * The type Git hub config.
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
     * The Token.
     */
    String token = "GITHUB_TOKEN";


}
