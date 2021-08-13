package com.example.demo.service.cloudformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.core.Environment;
import com.example.demo.core.Label;
import com.example.demo.core.LookupType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.ssm.StringParameter;

/**
 * Service for managing CloudFormation outputs.
 *
 * See <a href="https://docs.aws.amazon.com/cdk/latest/guide/resources.html">here</a> for default behaviour.
 *
 * Consuming outputs in stacks:
 *
 * To remove the FK constraints between stacks we export resource attributes to AWS ParameterStore under a well-known
 * uri using the stack name. These resources can be imported using
 * {@link com.example.demo.repository.IResourceRepository#lookup(Construct, String, LookupType)}.
 *
 * Consuming outputs in pipeline:
 *
 * Pre and post pipeline stage steps are usually implemented with some sort of "serverless" architecture e.g CodeBuild
 * or Lambda. We can create an environment map enabling these services to consume any stack output (from the current
 * environment stage) to perform tasks that are imperative and shouldn't be described "in code".
 */
@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StackOutputService {

    private final Map<Environment, List<CfnOutput>> cfnOutputs = new HashMap<>();
    private final Map<Environment, List<StringParameter>> ssmOutputs = new HashMap<>();

    public void addOutput(Construct scope, Environment stage, CfnOutput cfnOutput) {
        log.debug("Recording output {} for stage {}", cfnOutput.getExportName(), stage.name());

        cfnOutputs.computeIfAbsent(stage, k -> new ArrayList<>());
        cfnOutputs.merge(stage, Collections.singletonList(cfnOutput), (list1, list2) ->
                Stream.of(list1, list2)
                      .flatMap(Collection::stream)
                      //.peek(cfnOutput1 -> log.debug(cfnOutput1.getExportName()))
                      .collect(Collectors.toList()));

        ssmOutputs.computeIfAbsent(stage, k -> new ArrayList<>());

        StringParameter param = StringParameter.Builder
                .create(scope, String.format("%s%s", "SSMParam", cfnOutput.getNode().getId()))
                .parameterName(
                        Label.builder()
                             .namespace(Stack.of(scope).getStackName())
                             .resource(cfnOutput.getNode().getId())
                             .build()
                             .toSsmParameterName()
                )
                .stringValue((String) cfnOutput.getValue())
                .build();

        ssmOutputs
                .get(stage)
                .stream()
                .filter(stringParameter -> Stack.of(stringParameter).equals(Stack.of(scope)))
                .reduce((first, second) -> second)
                .ifPresent(last -> param.getNode().addDependency(last));

        ssmOutputs.merge(stage, Collections.singletonList(param), (list1, list2) ->
                Stream.of(list1, list2)
                      .flatMap(Collection::stream)
                      .collect(Collectors.toList()));
    }

    public Map<String, CfnOutput> toEnvVars(Environment stage) {
        log.info("Generating env vars for stage {}", stage.name());

        cfnOutputs.computeIfAbsent(stage, k -> new ArrayList<>());

        Map<String, CfnOutput> envVars = cfnOutputs.get(stage).stream()
                                                   //.peek(cfnOutput -> log.debug(cfnOutput.getExportName()))
                                                   .collect(Collectors.toMap(cfnOutput -> Objects
                                                           .requireNonNull(cfnOutput.getExportName())
                                                           .replaceAll("-", "_").toUpperCase(), cfnOutput -> cfnOutput));

        log.debug(envVars.keySet());

        return envVars;
    }

}