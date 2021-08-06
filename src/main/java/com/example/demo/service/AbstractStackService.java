package com.example.demo.service;

import com.example.demo.config.Environment;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

public abstract class AbstractStackService implements IStackService {

    public abstract Stack provision(Construct scope, String namespace, Environment stage);

}
