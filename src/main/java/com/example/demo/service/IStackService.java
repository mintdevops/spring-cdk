package com.example.demo.service;

import java.util.List;

import com.example.demo.config.Environment;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

public interface IStackService {

    Stack provision(Construct scope, String namespace, Environment stage);

}
