package com.example.demo.core;

/**
 * Simple enum to define the available environments in the framework. Useful for making decisions based on current
 * stage in the pipeline.
 */
public enum Environment {
    DEMO,
    CICD,
    DEV,
    BUILD,
    TEST,
    PERF,
    PROD
}
