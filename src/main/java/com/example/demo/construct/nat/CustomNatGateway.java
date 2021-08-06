package com.example.demo.construct.nat;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;

@Log4j2
@Getter
public class CustomNatGateway extends Construct {

    private final NatGatewayProvider natProvider;

    public CustomNatGateway(@NotNull Construct scope, @NotNull String id, NatGatewayConfig conf) {
        super(scope, id);

        natProvider = new NatGatewayProvider(scope, conf.getAllocationIds());
    }
}
