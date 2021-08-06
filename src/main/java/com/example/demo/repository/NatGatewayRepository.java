package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.Environment;
import com.example.demo.config.LookupType;
import com.example.demo.config.VpcConfig;
import com.example.demo.construct.nat.CustomNatGateway;
import com.example.demo.construct.nat.NatGatewayConfig;
import com.example.demo.construct.nat.NatGatewayProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.CfnRefElement;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.ec2.IVpc;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NatGatewayRepository extends AbstractResourceRepository<CustomNatGateway, NatGatewayConfig> {

    private final static String RESOURCE_NAME = "NAT";

    @Override
    public CustomNatGateway create(Construct scope, String namespace, Environment stage, NatGatewayConfig conf) {
        log.debug("create");
        return new CustomNatGateway(scope, RESOURCE_NAME, conf);
    }

    @Override
    public CustomNatGateway lookup(Construct scope, String stackName, LookupType lookupType) {
        throw new NotImplementedException();
    }

    public List<CfnOutput> export(Construct scope, CustomNatGateway resource) {
        List<CfnOutput> outputs = new ArrayList<>();

        outputs.add(createOutput(scope, "NATGatewayEIP", "Public IP address of NAT gateways",
                resource.getNatProvider().getEips().stream().map(CfnRefElement::getRef).collect(Collectors.joining(","))));

        return outputs;
    }
}
