package com.example.demo.construct.immutableserver;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
import software.amazon.awscdk.services.ec2.GenericLinuxImage;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.S3DownloadOptions;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.s3.assets.Asset;

@Getter
@Log4j2
public class ImmutableServer extends Construct {

    AutoScalingGroup asg;

    public static List<String> deployCommands() {
        return Arrays.asList();
    }

    public static List<String> confCommands() {
        return Arrays.asList();
    }

    public ImmutableServer(software.constructs.@NotNull Construct scope, @NotNull String id,
                           ImmutableServerSpec conf) {
        super(scope, id);

        Map<String, String> amiMap = new HashMap<>();
        amiMap.put(conf.getImageRegion(), conf.getImageId());

        Role role =
                Role.Builder
                        .create(this, "AppRole")
                        .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                        .managedPolicies(Arrays.asList(
                                ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")
                        ))
                        .build();

        SecurityGroup securityGroup =
                SecurityGroup.Builder.create(this, "AppSecurityGroup")
                                     .allowAllOutbound(true)
                                     .vpc(conf.getVpc())
                                     .build();

        // TODO: Support custom ingress/egress rules
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(8080));

        // TODO: Config
        UserData userData = UserData.forLinux();

        asg = AutoScalingGroup.Builder
                .create(this, "Asg")
                .vpc(conf.getVpc())
                .role(role)
                .securityGroup(securityGroup)
                .instanceType(InstanceType.of(conf.getAsg().getInstanceClass(), conf.getAsg().getInstanceSize()))
                .minCapacity(conf.getAsg().getMinSize())
                .maxCapacity(conf.getAsg().getMaxSize())
                .desiredCapacity(conf.getAsg().getDesiredSize())
                .machineImage(MachineImage.genericLinux(amiMap))
                .userData(userData)
                .build();
    }

    private void stageArtifacts() {
//        if (this.config.getStage() == Environment.DEV) {
//            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//            URL resource = classLoader.getResource(String.format("%s.%s", config.getArtifact().getArtifactName(),
//                    config.getArtifact().getArtifactType()));
//
//            if (resource == null) {
//                log.error("No artifact found on classpath with name {}", String.format("%s.%s", config.getArtifact().getArtifactName(),
//                        config.getArtifact().getArtifactType()));
//
//                throw new IllegalStateException();
//            }
//
//            File file = new File(resource.getPath());
//            artifact = Asset.Builder.create(this, "Artifact").path(file.getAbsolutePath()).build();
//            String localPath = userData.addS3DownloadCommand(S3DownloadOptions.builder()
//                                                                              .bucket(artifact.getBucket())
//                                                                              .bucketKey(artifact.getS3ObjectKey())
//                                                                              .build()
//            );
//            userData.addCommands(String.format("cp %s %s/%s.%s", localPath, config.getArtifact().getDeployRoot(),
//                    config.getArtifact().getDeployName(), config.getArtifact().getArtifactType()
//            ));
//            // TODO: Use resource signal to detect when application is up
//        }
    }
}
