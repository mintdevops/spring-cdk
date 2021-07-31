package com.example.demo.repository;

import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.construct.imagebuilder.AnsibleImageBuilder;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;

@Component
@Log4j2
public class ImageBuilderFactory {

    private final static String RESOURCE_NAME = "ImageBuilder";

    public IImageBuilder create(Construct parent, ImageBuilderConfig conf, Environment stage) {
        log.debug("ImageBuilder:create");
        log.debug(stage);
        log.debug(conf);

        // Options configurable ofc, with sensible defaults

        return new AnsibleImageBuilder(parent, RESOURCE_NAME,
                ImageBuilderConfig.builder()
                                  .vpcId(conf.getVpcId())
                                  .availabilityZones(conf.getAvailabilityZones())
                                  .subnetId(conf.getSubnetId())
                                  .imageName(conf.getImageName())
                                  .accounts(conf.getAccounts())
                                  .regions(conf.getRegions())
                                  .build());
    }

    // standard setters and getters
}