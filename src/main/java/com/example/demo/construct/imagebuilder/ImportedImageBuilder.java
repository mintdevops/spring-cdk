package com.example.demo.construct.imagebuilder;

import org.jetbrains.annotations.NotNull;

import software.constructs.Construct;

public class ImportedImageBuilder extends AbstractImageBuilder {

    public ImportedImageBuilder(@NotNull Construct scope, @NotNull String id, ImageAttributes props) {
        super(scope, id);

        setAmiId(props.getApplicationName());
    }

}
