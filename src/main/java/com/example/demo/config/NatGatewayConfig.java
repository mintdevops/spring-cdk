package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Externalised configuration for the NAT gateway configuration.
 */
@Data
@Component
@Validated
public class NatGatewayConfig {

    /**
     * A list of pre-existing allocation IDs to assign to the NAT gateway
     */
    private List<String> allocationIds = new ArrayList<>();
    /**
     * A threshold above which to trigger an alarm on the amount of bytes sent out (chargeable by AWS).
     *
     * Uses
     * <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/unit/DataSize.html">DataSize</a>
     * to convert between human readable formats and internal representations.
     */
    private String egressThreshold = "1GB";

}
