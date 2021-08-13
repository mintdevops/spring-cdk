package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.core.Environment;
import com.example.demo.core.LookupType;
import com.example.demo.construct.natgateway.CustomNatGateway;
import com.example.demo.construct.natgateway.NatGatewayConfig;
import com.example.demo.core.Label;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.CfnRefElement;
import software.amazon.awscdk.core.Construct;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * See {@link IResourceRepository} for more information.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NatGatewayRepository extends AbstractResourceRepository<CustomNatGateway, NatGatewayConfig> {

    private final static String RESOURCE_NAME = "NAT";

    @Override
    public CustomNatGateway create(Construct scope, String namespace, Environment stage, NatGatewayConfig conf) {
        return new CustomNatGateway(scope, Label.builder()
                                                .namespace(namespace)
                                                .stage(stage)
                                                .resource(RESOURCE_NAME)
                                                .build()
                                                .toLogicalId(), conf);
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
