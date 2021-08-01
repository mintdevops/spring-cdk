package com.example.demo.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.config.Label;
import com.example.demo.construct.imagebuilder.AnsibleImageBuilder;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;
import com.example.demo.service.LookupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.IVpc;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageBuilderFactory {

    private final static String RESOURCE_NAME = "ImageBuilder";

    private final AppConfig conf;
    private final LookupService lookupService;

    public IImageBuilder create(Construct parent, Environment stage) {
        log.debug("create");

        ImageBuildConfig imageConf = conf.getEnv().get(stage).getImage();

        IVpc vpc = lookupService.lookupVpcAtDeployByStackName(parent, imageConf.getVpcStackName());

        return new AnsibleImageBuilder(parent, Label.builder()
                                                    .namespace("")
                                                    .stage("")
                                                    .resource(RESOURCE_NAME)
                                                    .build()
                                                    .toString(),
                ImageBuilderConfig.builder()
                                  .vpcId(vpc.getVpcId())
                                  .availabilityZones(vpc.getAvailabilityZones())
                                  .subnetId(vpc.getPrivateSubnets().stream().findFirst().orElseThrow(IllegalArgumentException::new).getSubnetId())
                                  .imageName(imageConf.getImageName())
                                  .accounts(imageConf.getAccounts())
                                  .regions(imageConf.getRegions())
                                  .build());

    }

}