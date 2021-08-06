package com.example.demo.construct.imagebuilder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.example.demo.construct.imagebuilder.spec.Component;
import com.example.demo.construct.imagebuilder.spec.Inputs;
import com.example.demo.construct.imagebuilder.spec.Phases;
import com.example.demo.construct.imagebuilder.spec.Steps;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnResource;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.services.ec2.AmazonLinuxGeneration;
import software.amazon.awscdk.services.ec2.AmazonLinuxImageProps;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcAttributes;
import software.amazon.awscdk.services.iam.CfnInstanceProfile;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.imagebuilder.CfnComponent;
import software.amazon.awscdk.services.imagebuilder.CfnDistributionConfiguration;
import software.amazon.awscdk.services.imagebuilder.CfnImage;
import software.amazon.awscdk.services.imagebuilder.CfnImagePipeline;
import software.amazon.awscdk.services.imagebuilder.CfnImageProps;
import software.amazon.awscdk.services.imagebuilder.CfnImageRecipe;
import software.amazon.awscdk.services.imagebuilder.CfnInfrastructureConfiguration;
import software.amazon.awscdk.services.s3.assets.Asset;

@Log4j2
@Getter
@Setter(AccessLevel.PROTECTED)
public class AbstractImageBuilder extends Construct implements IImageBuilder {

    private String pipelineArn;
    private String amiId;

    public AbstractImageBuilder(software.constructs.@NotNull Construct scope, @NotNull String id) {
        super(scope, id);
    }

}
