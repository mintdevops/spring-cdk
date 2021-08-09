package com.example.demo.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;

@Data
@NoArgsConstructor
public class AsgConfig {

    String applicationName;
    String vpcStackName;
    String imageStackName;
    InstanceClass instanceClass = InstanceClass.BURSTABLE3;
    InstanceSize instanceSize = InstanceSize.NANO;
    Integer minSize;
    Integer maxSize;
    Integer desiredSize;

}
