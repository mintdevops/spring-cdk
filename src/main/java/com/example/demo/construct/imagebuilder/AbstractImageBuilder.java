package com.example.demo.construct.imagebuilder;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;

@Log4j2
@Getter
@Setter(AccessLevel.PROTECTED)
public class AbstractImageBuilder extends Construct implements IImageBuilder {

    private String pipelineArn;
    private String amiId;

    public AbstractImageBuilder(software.constructs.@NotNull Construct scope, @NotNull String id) {
        super(scope, id);
    }

    public static IImageBuilder fromImageAttributes(Construct scope, String id, ImageAttributes props) {
        return new ImportedImageBuilder(scope, id, props);
    }

}
