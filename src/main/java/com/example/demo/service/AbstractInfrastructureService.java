package com.example.demo.service;

import com.example.demo.core.Environment;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

/**
 * See {@link IInfrastructureService} for more information.
 */
public abstract class AbstractInfrastructureService implements IInfrastructureService {

    public abstract Stack provision(Construct scope, String namespace, Environment stage);

}
