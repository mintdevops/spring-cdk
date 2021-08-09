package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.vpc.nat")
@Validated
public class NatGatewayConfig {

    private List<String> allocationIds = new ArrayList<>();
    private String egressThreshold = "1GB";

}
