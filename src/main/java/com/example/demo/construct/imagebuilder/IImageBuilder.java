package com.example.demo.construct.imagebuilder;

import software.amazon.awscdk.core.IConstruct;

public interface IImageBuilder extends IConstruct {

    String getPipelineArn();
    String getAmiId();

}
