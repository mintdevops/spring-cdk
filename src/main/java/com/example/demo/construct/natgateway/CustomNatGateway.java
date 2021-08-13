package com.example.demo.construct.natgateway;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.unit.DataSize;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.services.cloudwatch.Alarm;
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator;
import software.amazon.awscdk.services.cloudwatch.Metric;
import software.amazon.awscdk.services.cloudwatch.Unit;
import software.amazon.awscdk.services.ec2.GatewayConfig;

@Log4j2
@Getter
public class CustomNatGateway extends Construct {

    private final NatGatewayProvider natProvider;

    public CustomNatGateway(@NotNull Construct scope, @NotNull String id, NatGatewayConfig conf) {
        super(scope, id);

        natProvider = new NatGatewayProvider(scope, conf.getAllocationIds());

        addEgressAlarm(natProvider, conf.getEgressThreshold());
    }

    /**
     * Trigger an alarm if the traffic over a 15 minute period on your NAT gateways exceeds a configurable threshold.
     * <p>
     * This can be a sign that an instance is downloading large amounts of data from the internet e.g. package
     * updates or your customer traffic is increasing.
     * <p>
     * The alarm should trigger your incident process to investigate the cause.
     *
     * @param thresholdInGb Human readable size in GB e.g. 1GB
     */
    private void addEgressAlarm(NatGatewayProvider gw, String thresholdInGb) {
        log.debug("addEgressAlarm");

        if (thresholdInGb == null || thresholdInGb.isEmpty()) {
            throw new IllegalStateException("No NAT gateway alarm threshold defined");
        }

        long thresholdInBytes = DataSize.parse(thresholdInGb).toBytes();

        // FIXME: Use logical name of NAT gateway
        int i = 0;
        for (GatewayConfig c : gw.getConfiguredGateways()) {
            String metricName = String.format("%sBytesOutToDestination",  i);
            Map<String, String> dimensions = new HashMap<>();
            dimensions.put("NatGatewayId", c.getGatewayId());

            Metric metric =
                    Metric.Builder
                            .create()
                            .metricName(metricName)
                            .namespace("AWS/NATGateway")
                            .dimensionsMap(dimensions)
                            .period(Duration
                                    .minutes(15))
                            .unit(Unit.BYTES)
                            .build();

            Alarm.Builder
                    .create(this, metricName)
                    .metric(metric)
                    .threshold(thresholdInBytes)
                    .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
                    .evaluationPeriods(1)
                    .build();

            i++;
        }
    }
}
