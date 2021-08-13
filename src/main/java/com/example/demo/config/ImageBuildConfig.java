package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Externalised configuration for the golden image pipeline.
 */
@Data
@Component
//@ConfigurationProperties(prefix = "app.image")
@Validated
public class ImageBuildConfig {

    /**
     * The Vpc stack name. This is where the image automation will run.
     */
    String vpcStackName;
    /**
     * The image name. This is the name given to the AMI produced by the image pipeline.
     */
    @NotEmpty
    String imageName;
    /**
     * A list of accounts to distribute the golden image to.
     */
    List<@Pattern(regexp = "(\\d{10})") String> accounts = new ArrayList<>();
    /**
     * A list of regions to distribute the golden image to.
     */
    // TODO: Validate list of available regions
    List<String> regions = new ArrayList<>();


}
