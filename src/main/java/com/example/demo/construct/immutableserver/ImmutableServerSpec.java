package com.example.demo.construct.immutableserver;

import java.util.List;

import com.example.demo.config.AsgConfig;
import com.example.demo.construct.ISpec;

import lombok.Builder;
import lombok.Data;
import software.amazon.awscdk.services.ec2.IVpc;

@Data
@Builder
public class ImmutableServerSpec implements ISpec {

    IVpc vpc;
    String imageId;
    String imageRegion;
    AsgConfig asg;
    List<String> deployCommands;
}
