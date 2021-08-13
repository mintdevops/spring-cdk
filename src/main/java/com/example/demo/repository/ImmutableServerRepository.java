package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;
import com.example.demo.construct.immutableserver.ImmutableServer;
import com.example.demo.construct.immutableserver.ImmutableServerSpec;
import com.example.demo.core.Label;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * See {@link IResourceRepository} for more information.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImmutableServerRepository extends AbstractResourceRepository<ImmutableServer, ImmutableServerSpec> {

    private final static String RESOURCE_NAME = "App";

    @Override
    public ImmutableServer create(Construct scope, String namespace, Environment stage, ImmutableServerSpec conf) {
        return new ImmutableServer(scope, Label.builder()
                                               .namespace(namespace)
                                               .stage(stage)
                                               .resource(RESOURCE_NAME)
                                               .build()
                                               .toLogicalId(), conf);
    }

    @Override
    public ImmutableServer lookup(Construct scope, String stackName, LookupType lookupType) {
        throw new NotImplementedException();
    }

    @Override
    public List<CfnOutput> export(Construct scope, ImmutableServer resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        //outputs.add(createOutput(scope, "AsgArn", "The AutoScalingGroup Arn",
                //resource.getAsg().getAutoScalingGroupArn()));
        outputs.add(createOutput(scope, "AsgName", "The AutoScalingGroup Name",
                resource.getAsg().getAutoScalingGroupName()));

        return outputs;
    }
}
