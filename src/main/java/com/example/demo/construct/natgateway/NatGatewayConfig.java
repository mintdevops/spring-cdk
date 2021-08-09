package com.example.demo.construct.natgateway;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.repository.IResourceConfig;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NatGatewayConfig implements IResourceConfig {

    private List<String> allocationIds = new ArrayList<>();
    private String egressThreshold;

}
