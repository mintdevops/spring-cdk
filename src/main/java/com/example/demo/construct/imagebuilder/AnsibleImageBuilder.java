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

import lombok.Data;
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
@Data
public class AnsibleImageBuilder extends Construct implements IImageBuilder {

    private static final String RESOURCE_DIR = "ansible";

    public String pipelineArn;
    public String amiId;

    public AnsibleImageBuilder(software.constructs.@NotNull Construct scope, @NotNull String id, ImageBuilderConfig props) {
        super(scope, id);

        // Stage the playbook in S3
        Asset playbook = this.stagePlaybook();

        // Generate the automation document
        Component component = this.buildComponent(playbook.getS3ObjectUrl());

        // Create a custom role for the automation
        Role role =
                Role.Builder
                        .create(this, "AutomationRole")
                        .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                        .managedPolicies(Arrays.asList(
                                ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"),
                                ManagedPolicy.fromAwsManagedPolicyName("EC2InstanceProfileForImageBuilder")
                        ))
                        .build();

        CfnInstanceProfile profile =
                CfnInstanceProfile.Builder
                        .create(this, "AutomationInstanceProfile")
                        .instanceProfileName(role.getRoleName())
                        .roles(Collections.singletonList(role.getRoleName()))
                        .build();

        // Grant read permissions to the CDK asset from the role
        playbook.grantRead(role);

        // Create a default security group to attach to the EC2 instance
        SecurityGroup securityGroup =
                SecurityGroup.Builder.create(this, "DefaultSecurityGroup")
                                     .allowAllOutbound(true)
                                     .vpc(Vpc.fromVpcAttributes(this, "Vpc",
                                             VpcAttributes
                                                     .builder()
                                                     .vpcId(props.vpcId)
                                                     .availabilityZones(props.availabilityZones)
                                                     .build()))
                                     .build();

        // Usually we need to reach the internet to download packages
        // TODO: Support custom ingress/egress rules
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443));

        // Create the component
        try {
            CfnComponent cfnComponent = CfnComponent.Builder.create(this, "AnsibleComponent")
                                                            .name(String.format("%s-ansible-playbook",
                                                                    props.getImageName()))
                                                            .platform("Linux")
                                                            .version(String.format("1.0.%s",
                                                                    Math.abs(component.hashCode())))
                                                            .data(component.toDocument())
                                                            .build();

            // Create the recipe (collection of build and test components)
            // TODO: Support Machine image configuration
            CfnImageRecipe cfnImageRecipe =
                    CfnImageRecipe.Builder.create(this, "Recipe")
                                          .name(props.getImageName())
                                          .parentImage(MachineImage
                                                  .latestAmazonLinux(AmazonLinuxImageProps
                                                          .builder()
                                                          .generation(AmazonLinuxGeneration.AMAZON_LINUX_2)
                                                          .build())
                                                  .getImage(this)
                                                  .getImageId())
                                          .version(String.format("1.0.%s",
                                                  Math.abs(component.hashCode())))
                                          .components(
                                                  Collections.singletonList(
                                                          CfnImageRecipe.ComponentConfigurationProperty
                                                                  .builder()
                                                                  .componentArn(cfnComponent.getAttrArn())
                                                                  .build()))
                                          .build();

            // Create an infrastructure configuration for the automation
            // TODO: Support EC2 configuration
            CfnInfrastructureConfiguration infra =
                    CfnInfrastructureConfiguration.Builder
                            .create(this, "InfraConfig")
                            .name(props.getImageName())
                            .instanceTypes(Collections.singletonList("m5.large"))
                            .subnetId(props.subnetId)
                            .securityGroupIds(Collections.singletonList(securityGroup.getSecurityGroupId()))
                            .instanceProfileName(profile.getInstanceProfileName())
                            .build();

            // Create the EC2 pipeline out of all the resources
            CfnImagePipeline pipeline =
                    CfnImagePipeline.Builder
                            .create(this, "ImagePipeline")
                            .name(props.getImageName())
                            .imageRecipeArn(cfnImageRecipe.getAttrArn())
                            .infrastructureConfigurationArn(infra.getAttrArn())
                            .build();

            // Prevent EC2 image pipeline from triggering until dependent resources have been created
            infra.addDependsOn(profile);
            pipeline.addDependsOn(infra);

            pipelineArn = pipeline.getAttrArn();

            List<Distribution> distributions = new ArrayList<>();
            for (String a : props.getAccounts()) {
                for (String r : props.getRegions()) {
                    Distribution d =Distribution.builder()
                                                .region(r)
                                                .amiDistributionConfiguration(
                                                        AmiDistributionConfiguration.builder()
                                                                                    .name(String.format("%s_{{ imagebuilder:buildDate }}",props.getImageName()))
                                                                                    .description(props.getImageName())
                                                                                    .launchPermissionConfiguration(
                                                                                            LaunchPermission.builder()
                                                                                                            .userIds(Collections.singletonList(a))
                                                                                                            .build()
                                                                                    )
                                                                                    .build()
                                                )
                                                .build();
                    distributions.add(d);
                }
            }


            // Distribute to accounts/regions
            CfnDistributionConfiguration cfnDistributionConfiguration =
                    CfnDistributionConfiguration.Builder
                            .create(this, "Distribution")
                            .description(props.getImageName())
                            .name(props.getImageName())
                            .distributions(distributions)
                            .build();

            // Create a dependency between the pipeline execution and an image resource to automatically trigger
            // the pipeline whenever the build scripts change
            CfnImage image = new CfnImage(this, "Ami", CfnImageProps.builder()
                                                                    .imageRecipeArn(cfnImageRecipe.getAttrArn())
                                                                    .infrastructureConfigurationArn(infra.getAttrArn())
                                                                    .enhancedImageMetadataEnabled(true)
                                                                    .distributionConfigurationArn(cfnDistributionConfiguration.getAttrArn()).build());

            //log.debug(image.getNode().getChildren());
            ((CfnResource) image).applyRemovalPolicy(RemovalPolicy.RETAIN);

//            StringParameter param = StringParameter.Builder.create(this, "AmiId")
//                                                           .type(ParameterType.AWS_EC2_IMAGE_ID)
//                                                           .parameterName(String.format("/%s/AmiId", Stack
//                                                                   .of(this)
//                                                                   .getStackName()))
//                                                           .stringValue(image.getAttrImageId())
//                                                           .description("Machine image produced by EC2 image pipeline")
//                                                           .build();

            amiId = image.getAttrImageId();



        } catch (JsonProcessingException e) {
            e.printStackTrace();

            throw new IllegalStateException();
        }
    }

    private Asset stagePlaybook() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(RESOURCE_DIR);

        if (resource == null) {
            log.error("No ansible directory found on classpath");

            throw new IllegalStateException();
        }

        File file = new File(resource.getPath());

        return Asset.Builder.create(this, "Playbook").path(file.getAbsolutePath()).build();
    }

    private Component buildComponent(String s3Uri) {
        return Component.builder()
                        .name("ansible-playbook")
                        .description("Execute an Ansible playbook")
                        .schemaVersion("1.0")
                        .phases(
                                Arrays.asList(
                                        Phases.builder()
                                              .name("build")
                                              .steps(
                                                      Arrays.asList(
                                                              Steps.builder()
                                                                   .name("InstallAnsible")
                                                                   .action("ExecuteBash")
                                                                   .inputs(
                                                                           Inputs.builder()
                                                                                 .commands(
                                                                                         Arrays.asList(
                                                                                                 "sudo yum install -y amazon-linux-extras",
                                                                                                 "sudo amazon-linux-extras enable ansible2",
                                                                                                 "sudo yum install -y ansible"
                                                                                         )
                                                                                 )
                                                                                 .build()
                                                                   )
                                                                   .build(),
                                                              Steps.builder()
                                                                   .name("CreateWorkingDir")
                                                                   .action("ExecuteBash")
                                                                   .inputs(
                                                                           Inputs
                                                                                   .builder()
                                                                                   .commands(
                                                                                           Collections.singletonList(
                                                                                                   "mkdir /tmp/ec2-golden-image"
                                                                                           )
                                                                                   )
                                                                                   .build())
                                                                   .build(),
                                                              Steps.builder()
                                                                   .name("DownloadPlaybook")
                                                                   .action("S3Download")
                                                                   .inputs(
                                                                           Inputs
                                                                                   .builder()
                                                                                   .source(s3Uri)
                                                                                   .destination("/tmp/ec2-golden-image/ansible.zip")
                                                                                   .build())
                                                                   .build(),
                                                              Steps.builder()
                                                                   .name("UnzipPlaybook")
                                                                   .action("ExecuteBash")
                                                                   .inputs(
                                                                           Inputs.builder()
                                                                                 .commands(
                                                                                         Collections.singletonList("unzip /tmp/ec2-golden-image/ansible.zip -d /tmp/ec2-golden-image")
                                                                                 )
                                                                                 .build()
                                                                   )
                                                                   .build(),
                                                              Steps.builder()
                                                                   .name("InvokePlaybook")
                                                                   .action("ExecuteBinary")
                                                                   .inputs(
                                                                           Inputs.builder()
                                                                                 .path("ansible" +
                                                                                         "-playbook")
                                                                                 .arguments(
                                                                                         Collections.singletonList("/tmp/ec2-golden-image/playbook.yml")
                                                                                 )
                                                                                 .build()
                                                                   )
                                                                   .build(),
                                                              Steps.builder()
                                                                   .name("DeletePlaybook")
                                                                   .action("ExecuteBash")
                                                                   .inputs(
                                                                           Inputs.builder()
                                                                                 .commands(Collections.singletonList("rm -rf /tmp/ec2-golden-image"))
                                                                                 .build())
                                                                   .build()
                                                      )
                                              )
                                              .build()
                                )
                        )
                        .build();
    }
}
