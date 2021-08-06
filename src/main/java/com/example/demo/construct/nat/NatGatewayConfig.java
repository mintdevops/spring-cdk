package com.example.demo.construct.nat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.example.demo.repository.IResourceConfig;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NatGatewayConfig implements IResourceConfig {

    private List<String> allocationIds = new ArrayList<>();

}
