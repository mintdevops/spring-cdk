package com.example.demo.repository;

import java.util.List;

import com.example.demo.construct.ISpec;
import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;
import com.example.demo.core.Label;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.IConstruct;
import software.amazon.awscdk.core.Stack;


/**
 * See {@link IResourceRepository} for more information.
 *
 * @param <R> the type parameter
 * @param <C> the type parameter
 */
public abstract class AbstractResourceRepository<R extends IConstruct, C extends ISpec> implements IResourceRepository<R, C> {

    public abstract R create(Construct scope, String namespace, Environment stage, C conf);

    public abstract R lookup(Construct scope, String stackName, LookupType lookupType);

    public abstract List<CfnOutput> export(Construct scope, R resource);

    /**
     * Simplify the creation of CfnOutputs.
     *
     * @param scope       the scope
     * @param name        the name
     * @param description the description
     * @param value       the value
     * @return the cfn output
     */
    protected CfnOutput createOutput(Construct scope, String name, String description, String value) {
        return CfnOutput.Builder
                .create(scope, name)
                .description(description)
                .value(value)
                .exportName(
                        Label.builder()
                             .namespace(Stack.of(scope).getStackName())
                             .resource(name)
                             .build()
                             .toCfnExport())
                .build();
    }

}
