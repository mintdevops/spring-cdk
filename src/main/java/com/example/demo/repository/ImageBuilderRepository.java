package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.Label;
import com.example.demo.core.LookupType;
import com.example.demo.construct.imagebuilder.AbstractImageBuilder;
import com.example.demo.construct.imagebuilder.AnsibleImageBuilder;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageAttributes;
import com.example.demo.construct.imagebuilder.ImageBuilderSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ssm.StringParameter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * See {@link IResourceRepository} for more information.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageBuilderRepository extends AbstractResourceRepository<IImageBuilder, ImageBuilderSpec> {

    private final static String RESOURCE_NAME = "Image";

    private final AppConfig conf;

    @Override
    public IImageBuilder create(Construct scope, String namespace, Environment stage, ImageBuilderSpec conf) {
        return new AnsibleImageBuilder(scope, Label.builder()
                                                    .namespace(namespace)
                                                    .stage(stage)
                                                    .resource(RESOURCE_NAME)
                                                    .build()
                                                    .toLogicalId(), conf);
    }

    @Override
    public IImageBuilder lookup(Construct scope, String stackName, LookupType lookupType) {
        if (conf.isDemo()) {
            log.debug("Creating demo ImageBuilder");

            return AbstractImageBuilder.fromImageAttributes(scope, "DemoImage",
                    ImageAttributes.builder()
                                   .applicationName("app-123")
                                   .build());
        }

        String imageId = StringParameter.valueForStringParameter(
                scope,
                Label.builder()
                     .namespace(stackName)
                     .resource("AmiId")
                     .build()
                     .toSsmParameterName()
        );

        return AbstractImageBuilder.fromImageAttributes(scope, stackName,
                ImageAttributes.builder()
                               .applicationName(imageId)
                               .build());
    }

    public List<CfnOutput> export(Construct scope, IImageBuilder resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "PipelineArn", "The Pipeline Arn", resource.getPipelineArn()));
        outputs.add(createOutput(scope, "AmiId", "The Image Id", resource.getAmiId()));

        return outputs;
    }
}
