package com.example.demo.config;

import software.amazon.awscdk.core.Construct;

public interface IStack {

    void setScope(Construct scope);
    void setEnv(Environment env);
    void setNamespace(String namespace);
    void provision();
    String getQualifier();

}
