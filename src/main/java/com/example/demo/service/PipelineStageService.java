package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;

@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineStageService {

    private Map<String, CfnOutput> outputs = new HashMap<>();

    public void addOutput(CfnOutput cfnOutput) {
        log.info("Creating env var from CfnOutput {}", cfnOutput.toString());

        String envVar = Objects.requireNonNull(cfnOutput.getExportName()).replaceAll("-", "_").toUpperCase();

        log.debug("CfnOutput {} available as {} in CodeBuild", cfnOutput.toString(), envVar);

        this.outputs.put(envVar, cfnOutput);
    }

}
