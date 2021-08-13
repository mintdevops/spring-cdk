package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Externalised configuration for the stack managed by the pipeline.
 *
 * NB: Spring Boot property binding does not support polymorphism but the pipeline can be used to manage
 * heterogeneous infrastructure so we must enumerate all possible workloads so that property binding works.
 *
 * The underlying stack can access the parts of the configuration it needs to provision the resources.
 *
 * TODO: Figure out how to support user-defined stacks
 */
@Data
@Component
@Validated
public class EnvConfig {

    /**
     * The Deploy.
     */
    //@Valid
    DeployConfig deploy = new DeployConfig();
    /**
     * The Vpc.
     */
    //@Valid
    VpcConfig vpc = new VpcConfig();
    /**
     * The Image.
     */
    //@Valid
    ImageBuildConfig image = new ImageBuildConfig();
    /**
     * The Asg.
     */
    //@Valid
    AsgConfig asg = new AsgConfig();
    /**
     * A set of tags to apply to all resources in the environment. Uses the workload qualifier.
     */
    Map<String, String> tags = new HashMap<>();

}