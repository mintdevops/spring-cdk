package com.example.demo.repository;

import java.util.List;

import com.example.demo.construct.ISpec;
import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.IConstruct;

/**
 * The resource repository interface definition.
 *
 * The repository pattern is a common Spring design pattern that sits at a higher level of abstraction than the DAO
 * pattern.
 *
 * Generally speaking in CDK we do not operate on a 1:1 resource mapping due to the internal architecture of
 * CDK. In simple terms L1 constructs e.g. CfnX can be thought of as directly accessing a table in a database whereas
 * L2+ constructs are usually aggregations of multiple resources e.g. a VPC consists of the VPC, security groups,
 * internet gateways, EIP's etc.
 *
 * Repositories act on L2+ constructs and act as a layer of abstraction to decouple constructs from their CRUD
 * operations.
 *
 * A good use case for the repository is that when working on CDK applications that use external resources defined
 * by another CDK app, raw CloudFormation, Terraform or even manually provisioned, the user would not be able to run
 * `cdk synth` unless the dependent resources existed which makes development a time consuming activity. By
 * decoupling the resources dependencies through the repository we can inject fake resources to allow `cdk synth` to
 * be run without any AWS credentials and allow development testing to take place in isolation.
 *
 * @param <R> the type parameter
 * @param <C> the type parameter
 */
public interface IResourceRepository<R extends IConstruct, C extends ISpec> {

    /**
     * Creates the resource in the construct tree.
     *
     * @param scope     the scope
     * @param namespace the namespace
     * @param stage     the stage
     * @param conf      the conf
     * @return the r
     */
    R create(Construct scope, String namespace, Environment stage, C conf);

    /**
     * Lookup a resource from an external source.
     *
     * @param scope      the scope
     * @param stackName  the stack name
     * @param lookupType the lookup type
     * @return the r
     */
    R lookup(Construct scope, String stackName, LookupType lookupType);

    /**
     * Export the resources attributes to CloudFormation.
     *
     * @param scope    the scope
     * @param resource the resource
     * @return the list
     */
    List<CfnOutput> export(Construct scope, R resource);

}
