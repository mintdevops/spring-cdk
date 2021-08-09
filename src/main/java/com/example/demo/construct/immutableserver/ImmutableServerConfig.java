package com.example.demo.construct.immutableserver;

import com.example.demo.config.AsgConfig;
import com.example.demo.repository.IResourceConfig;

import lombok.Builder;
import lombok.Data;
import software.amazon.awscdk.services.ec2.IVpc;

@Data
@Builder
public class ImmutableServerConfig implements IResourceConfig {

    IVpc vpc;
    String imageId;
    AsgConfig asg;
}
