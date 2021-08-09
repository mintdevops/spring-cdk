package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.LookupType;
import com.example.demo.construct.imagebuilder.AbstractImageBuilder;
import com.example.demo.construct.imagebuilder.AnsibleImageBuilder;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageAttributes;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageBuilderRepository extends AbstractResourceRepository<IImageBuilder, ImageBuilderConfig> {

    private final static String RESOURCE_NAME = "Image";

    private final AppConfig conf;

    @Override
    public IImageBuilder create(Construct scope, String namespace, Environment stage, ImageBuilderConfig conf) {
        return new AnsibleImageBuilder(scope, Label.builder()
                                                    .namespace("")
                                                    .stage("")
                                                    .resource(RESOURCE_NAME)
                                                    .build()
                                                    .toString(), conf);
    }

    @Override
    public IImageBuilder lookup(Construct scope, String stackName, LookupType lookupType) {
        log.debug("looking up from {}", stackName);

        if (conf.isDemo()) {
            return AbstractImageBuilder.fromImageAttributes(scope, "DemoImage",
                    ImageAttributes.builder().applicationName(stackName).build());
        }

        // TODO: Lookup in SSM by stack name
        return AbstractImageBuilder.fromImageAttributes(scope, "Image",
                ImageAttributes.builder().applicationName(stackName).build());
    }

    public List<CfnOutput> export(Construct scope, IImageBuilder resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "PipelineArn", "The Pipeline Arn", resource.getPipelineArn()));
        outputs.add(createOutput(scope, "AmiId", "The Image Id", resource.getAmiId()));

        return outputs;
    }
}
