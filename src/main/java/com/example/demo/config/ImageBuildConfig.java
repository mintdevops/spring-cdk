package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.repository.IResourceConfig;

import lombok.Data;

/**
 * The type Image build config v 2.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.image")
@Validated
public class ImageBuildConfig {

    /**
     * The Vpc stack name.
     */
    String vpcStackName;
    /**
     * The image name.
     */
    String imageName;
    /**
     * The Accounts.
     */
    List<String> accounts = new ArrayList<>();
    /**
     * The Regions.
     */
    List<String> regions = new ArrayList<>();


}
