package com.example.demo.core;

import com.example.demo.service.IInfrastructureService;

import software.amazon.awscdk.core.Construct;

/**
 * Simple enumeration of the stacks supported by the framework.
 *
 * Stacks are created by Spring beans using the
 * {@link IInfrastructureService#provision(Construct, String, Environment)}
 * to make the application more idiomatic to developers (in effect, the public API). However the pipeline stack
 * service infers which bean to use from {@link com.example.demo.config.PipelineConfig}
 *
 * TODO: At some point it may be better to have separate pipeline implementations for each archetype.
 */
public enum StackType {
    NETWORK,
    IMAGE,
    PIPELINE,
    WORKLOAD
}
