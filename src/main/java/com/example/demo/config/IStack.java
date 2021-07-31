package com.example.demo.config;

import software.amazon.awscdk.core.Construct;

public interface IStack {

    void setScope(Construct scope);
    void setEnvironment(Environment env);
    void provision();

}
