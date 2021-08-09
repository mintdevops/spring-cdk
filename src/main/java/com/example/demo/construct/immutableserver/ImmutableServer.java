package com.example.demo.construct.immutableserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
import software.amazon.awscdk.services.ec2.GenericLinuxImage;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;

@Getter
@Log4j2
public class ImmutableServer extends Construct {

    AutoScalingGroup asg;

    public ImmutableServer(software.constructs.@NotNull Construct scope, @NotNull String id,
                           ImmutableServerConfig conf) {
        super(scope, id);

        // TODO: Cross region support
        Map<String, String> amiMap = new HashMap<>();
        amiMap.put(Stack.of(this).getRegion(), conf.getImageId());

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

        UserData userData = UserData.forLinux();
        // TODO: Config

        asg = AutoScalingGroup.Builder
                .create(this, "Asg")
                .vpc(conf.getVpc())
                .role(role)
                .securityGroup(securityGroup)
                .instanceType(InstanceType.of(conf.getAsg().getInstanceClass(), conf.getAsg().getInstanceSize()))
                .minCapacity(conf.getAsg().getMinSize())
                .maxCapacity(conf.getAsg().getMaxSize())
                .desiredCapacity(conf.getAsg().getDesiredSize())
                .machineImage(GenericLinuxImage.Builder.create(amiMap).build())
                .userData(userData)
                .build();
    }
}
