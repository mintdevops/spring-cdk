package com.example.demo.construct.imagebuilder;

import java.util.List;

import com.example.demo.construct.ISpec;

import lombok.Builder;
import lombok.Data;
import software.amazon.awscdk.services.ec2.IVpc;

@Data
@Builder
public class ImageBuilderSpec implements ISpec {

    List<String> regions;
    List<String> accounts;
    IVpc vpc;
    String imageName;

}
