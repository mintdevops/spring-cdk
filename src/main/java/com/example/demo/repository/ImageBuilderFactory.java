package com.example.demo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.config.Label;
import com.example.demo.config.VpcConfig;
import com.example.demo.construct.imagebuilder.AnsibleImageBuilder;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;
import com.example.demo.svc.LookupService;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;

@Component
@Log4j2
public class ImageBuilderFactory {

    @Autowired
    AppConfig conf;

    @Autowired
    LookupService lookupService;

    private final static String RESOURCE_NAME = "ImageBuilder";

    public IImageBuilder create(Construct parent, Environment stage) {
        log.debug("create");

        ImageBuildConfig imageConf = conf.getEnv().get(stage).getImage();

        // Options configurable ofc, with sensible defaults

        return new AnsibleImageBuilder(parent, Label.builder()
                                                    .namespace("")
                                                    .stage("")
                                                    .resource(RESOURCE_NAME)
                                                    .build()
                                                    .toString(),
                ImageBuilderConfig.builder()
                                  .vpcId(lookupService.getVpcId())
                                  .availabilityZones(lookupService.getAvailabilityZones())
                                  .subnetId(lookupService.pickSubnet())
                                  .imageName(imageConf.getImageName())
                                  .accounts(imageConf.getAccounts())
                                  .regions(imageConf.getRegions())
                                  .build());
    }

    // standard setters and getters
}