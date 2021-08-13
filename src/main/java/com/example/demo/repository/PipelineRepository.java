package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.construct.pipeline.CustomPipeline;
import com.example.demo.construct.pipeline.PipelineSpec;
import com.example.demo.core.Environment;
import com.example.demo.core.Label;
import com.example.demo.core.LookupType;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.pipelines.CodePipeline;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineRepository extends AbstractResourceRepository<CustomPipeline, PipelineSpec> {

    private final static String RESOURCE_NAME = "Pipeline";

    @Override
    public CustomPipeline create(Construct scope, String namespace, Environment stage, PipelineSpec spec) {
        return new CustomPipeline(scope, Label.builder()
                                              .namespace(namespace)
                                              .stage(stage)
                                              .resource(RESOURCE_NAME)
                                              .build()
                                              .toLogicalId(), spec);
    }

    @Override
    public CustomPipeline lookup(Construct scope, String stackName, LookupType lookupType) {
        throw new NotImplementedException();
    }

    public List<CfnOutput> export(Construct scope, CustomPipeline resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "PipelineArn", "The Pipeline ARN", resource
                .getPipeline()
                .getPipeline().
                getPipelineArn()
        ));
        //TODO: Add console URL

        return outputs;
    }
}
