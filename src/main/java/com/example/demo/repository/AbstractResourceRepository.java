package com.example.demo.repository;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.config.Environment;
import com.example.demo.config.LookupType;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.IConstruct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ssm.StringParameter;

public abstract class AbstractResourceRepository<R extends IConstruct, C extends IResourceConfig> implements IResourceRepository<R, C> {

    public abstract R create(Construct scope, String namespace, Environment stage, C conf);

    public abstract R lookup(Construct scope, String stackName, LookupType lookupType);

    public abstract List<CfnOutput> export(Construct scope, R resource);

    // TODO: Move to StackOutputService
    public List<StringParameter> exportSSM(Construct scope, List<CfnOutput> outputs) {
        List<StringParameter> params = outputs
                .stream()
                .map(o -> StringParameter.Builder
                        .create(scope, String.format("%s%s", "SSMParam", o.getNode().getId()))
                        .parameterName(stackNameToSSMParam(Stack.of(scope).getStackName(), o.getNode().getId()))
                        .stringValue((String) o.getValue())
                        .build())
                .collect(Collectors.toList());

        // Workaround concurrency issues in SSM by serializing each parameter
        int i = 0;
        for (StringParameter param : params) {
            //params.add(param);
            if (i > 0 && i < params.size()) {
                param.getNode().addDependency(params.get(i - 1));
            }
            i++;
        }

        return params;
    }

    protected String stackNameToSSMParam(String stackName, String param) {
        return String.format("/%s/%s", stackName, param);
    }

    protected String stackNameToCFExport(String stackName, String param) {
        return String.format("%s-%s", stackName, param).replaceAll("/", "-");
    }

    protected CfnOutput createOutput(Construct scope, String name, String description, String value) {
        CfnOutput cfnOutput = CfnOutput.Builder
                .create(scope, name)
                .description(description)
                .value(value)
                .exportName(stackNameToCFExport(Stack.of(scope).getStackName(), name))
                .build();

        return cfnOutput;
    }

}
