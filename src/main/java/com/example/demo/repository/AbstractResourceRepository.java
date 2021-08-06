package com.example.demo.repository;

import java.util.List;

import com.example.demo.config.Environment;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.IConstruct;
import software.amazon.awscdk.core.IResource;
import software.amazon.awscdk.core.Stack;

public abstract class AbstractResourceRepository<R extends IConstruct, C extends IResourceConfig> implements IResourceRepository<R, C> {

    public abstract R create(Construct scope, String namespace, Environment stage, C conf);

    public abstract R lookup(Construct scope, String stackName, LookupType lookupType);

    public abstract List<CfnOutput> export(Construct scope, R resource);

    protected String stackNameToSSMParam(String stackName, String param) {
        return String.format("/%s/%s", stackName, param);
    }

    protected String stackNameToCFExport(String stackName, String param) {
        return String.format("%s-%s", stackName, param).replaceAll("/", "-");
    }

    protected CfnOutput createOutput(Construct scope, String name, String description, String value) {
        return CfnOutput.Builder
                .create(scope, name)
                .description(description)
                .value(value)
                .exportName(stackNameToCFExport(Stack.of(scope).getStackName(), name))
                .build();
    }

}
