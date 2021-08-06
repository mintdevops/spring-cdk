package com.example.demo.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.ImageBuildConfig;
import com.example.demo.config.Label;
import com.example.demo.construct.imagebuilder.AnsibleImageBuilder;
import com.example.demo.construct.imagebuilder.IImageBuilder;
import com.example.demo.construct.imagebuilder.ImageBuilderConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.IVpc;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ImageBuilderRepository extends AbstractResourceRepository<IImageBuilder, ImageBuildConfig> {

    private final static String RESOURCE_NAME = "Image";
    private final VpcRepository vpcRepository;

    @Override
    public IImageBuilder create(Construct scope, String namespace, Environment stage, ImageBuildConfig conf) {
        IVpc vpc = vpcRepository.lookup(scope, conf.getVpcStackName(), LookupType.DEPLOY);

        // TODO: Use factory
        return new AnsibleImageBuilder(scope, Label.builder()
                                                    .namespace("")
                                                    .stage("")
                                                    .resource(RESOURCE_NAME)
                                                    .build()
                                                    .toString(),
                ImageBuilderConfig.builder()
                                  .vpcId(vpc.getVpcId())
                                  .availabilityZones(vpc.getAvailabilityZones())
                                  .subnetId(vpc.getPrivateSubnets().stream().findFirst().orElseThrow(IllegalArgumentException::new).getSubnetId())
                                  .imageName(conf.getImageName())
                                  .accounts(conf.getAccounts())
                                  .regions(conf.getRegions())
                                  .build());
    }

    @Override
    public IImageBuilder lookup(Construct scope, String stackName, LookupType lookupType) {
        throw new IllegalStateException();
    }
    public List<CfnOutput> export(Construct scope, IImageBuilder resource) {
        throw new IllegalStateException();
    }
}
