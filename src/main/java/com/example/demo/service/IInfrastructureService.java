package com.example.demo.service;

import com.example.demo.core.Environment;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;

/**
 * The infrastructure service interface definition.
 *
 * The CDK architecture is not limited to AWS although its primary use case is for CloudFormation there are also
 * other projects for Terraform and Kubernetes. For this reason it is sensible to encapsulate these implementation
 * behind an interface for future proofing.
 *
 * We can define common, pre-configured stacks (in CloudFormation) without exposing
 * their internals to the Spring application. For example we could create a CodePipeline in AWS (or evenanother pipeline
 * provider) that deploys to both AWS using `cdk` and GCP using `cdkterraform` for a DR environment.
 *
 * Stacks should be thought of as being generally light weight. They should create, lookup and export infrastructure
 * resources created by repositories and not directly interact with your constructs.
 */
public interface IInfrastructureService {

    /**
     * Provision infrastructure resources.
     *
     * @param scope     the scope
     * @param namespace the namespace
     * @param stage     the stage
     * @return the stack
     */
    //FIXME: Should this be overloaded or replaced with a more generic CloudAssembly
    Stack provision(Construct scope, String namespace, Environment stage);

}
