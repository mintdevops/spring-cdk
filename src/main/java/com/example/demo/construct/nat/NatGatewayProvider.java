package com.example.demo.construct.nat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.AddRouteOptions;
import software.amazon.awscdk.services.ec2.CfnEIP;
import software.amazon.awscdk.services.ec2.CfnNatGateway;
import software.amazon.awscdk.services.ec2.ConfigureNatOptions;
import software.amazon.awscdk.services.ec2.GatewayConfig;
import software.amazon.awscdk.services.ec2.NatProvider;
import software.amazon.awscdk.services.ec2.PrivateSubnet;
import software.amazon.awscdk.services.ec2.PublicSubnet;
import software.amazon.awscdk.services.ec2.RouterType;

@Log4j2
public class NatGatewayProvider extends NatProvider {
    private final Construct construct;
    private final Map<String, String> gateways = new HashMap<>();
    private final List<String> allocationIds;
    private final List<CfnEIP> eips = new ArrayList<>();

    public NatGatewayProvider(Construct construct, List<String> allocationIds) {
        super();

        this.construct = construct;
        this.allocationIds = allocationIds;
    }

    @Override
    public void configureNat(@NotNull ConfigureNatOptions configureNatOptions) {
        if (!this.allocationIds.isEmpty() && configureNatOptions
                .getNatSubnets()
                .size() != this.allocationIds.size()) {
            log.error("Number of allocation IDs ({}) should match number of NAT gateways " +
                    "({})", this.allocationIds.size(), configureNatOptions.getNatSubnets().size());

            throw new IllegalArgumentException();
        }

        int i = 0;
        for (PublicSubnet s : configureNatOptions.getNatSubnets()) {
            CfnNatGateway gw;
            if (this.allocationIds.size() == configureNatOptions.getNatSubnets().size()) {
                // Assign existing EIP's
                gw =
                        CfnNatGateway.Builder
                                .create(this.construct, String.format("NATGateway%s", i))
                                .allocationId(this.allocationIds.get(i))
                                .subnetId(s.getSubnetId())
                                .build();
            } else {
                // Generate new EIP's
                CfnEIP eip = CfnEIP.Builder.create(this.construct, String.format("EIPNATGateway%s", i)).domain("vpc").build();

                gw = CfnNatGateway.Builder
                        .create(this.construct, String.format("NATGateway%s", i))
                        .allocationId(eip.getAttrAllocationId())
                        .subnetId(s.getSubnetId())
                        .build();

                this.eips.add(eip);
            }

            // TODO: Support user EIP
            this.gateways.put(s.getAvailabilityZone(), gw.getRef());
            i++;
        }

        for (PrivateSubnet s : configureNatOptions.getPrivateSubnets()) {
            this.configureSubnet(s);
        }
    }

    @Override
    public void configureSubnet(@NotNull PrivateSubnet privateSubnet) {
        String az = privateSubnet.getAvailabilityZone();
        String gwId = this.gateways.get(az);
        privateSubnet.addRoute("DefaultRoute",
                AddRouteOptions
                        .builder()
                        .routerType(RouterType.NAT_GATEWAY)
                        .routerId(gwId)
                        .enablesInternetConnectivity(true)
                        .build());
    }

    @Override
    public @NotNull List<GatewayConfig> getConfiguredGateways() {
        return this.gateways
                .entrySet()
                .stream()
                .map(e -> GatewayConfig.builder().az(e.getKey()).gatewayId(e.getValue()).build())
                .collect(Collectors.toList());
    }

    public List<CfnEIP> getEips() {
        return this.eips;
    }
}
