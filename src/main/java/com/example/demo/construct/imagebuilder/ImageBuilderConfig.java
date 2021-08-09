package com.example.demo.construct.imagebuilder;

import java.util.List;

import com.example.demo.repository.IResourceConfig;

import lombok.Builder;
import lombok.Data;
import software.amazon.awscdk.services.ec2.IVpc;

@Data
@Builder
public class ImageBuilderConfig implements IResourceConfig {

    List<String> regions;
    List<String> accounts;
    IVpc vpc;
    String imageName;

}
