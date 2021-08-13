package com.example.demo.config;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;

/**
 * Externalised configuration for an autoscaling group (usually as part of an immutable server deployment).
 */
@Data
@NoArgsConstructor
@Validated
public class AsgConfig {

    /**
     * The Application name.
     */
    @NotEmpty
    String applicationName;
    /**
     * The Vpc stack name.
     */
    @NotEmpty
    String vpcStackName;
    /**
     * The Image stack name.
     */
    // FIXME: Conditional validation - stack name not required after DEV
    String imageStackName;
    /**
     * The Instance class.
     */
    InstanceClass instanceClass = InstanceClass.BURSTABLE3;
    /**
     * The Instance size.
     */
    InstanceSize instanceSize = InstanceSize.NANO;
    /**
     * The Min size.
     */
    @Min(1)
    Integer minSize = 1;
    /**
     * The Max size.
     */
    @Max(10)
    Integer maxSize = 1;
    /**
     * The Desired size.
     */
    Integer desiredSize = 1;

}
