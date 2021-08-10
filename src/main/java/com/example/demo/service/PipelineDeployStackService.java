package com.example.demo.service;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.config.Label;
import com.example.demo.config.StackType;
import com.example.demo.core.pipeline.StackFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.iam.AccountPrincipal;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.RoleProps;

@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PipelineDeployStackService extends AbstractStackService {

    public static final StackType QUALIFIER = StackType.DEPLOY;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final StackOutputService stackOutputService;

    @Override
    public Stack provision(Construct scope, String namespace, Environment stage) {
        log.debug("provision");

        Stack stack = stackFactory.create(scope, namespace);

        addPublishAmiIdRole(stack, stage);
        addCreateEc2ImageRole(stack, stage);

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    private void addPublishAmiIdRole(Stack stack, Environment stage){
        // TODO: Refactor into repository
        PolicyStatement statement = new PolicyStatement(PolicyStatementProps.builder()
                                                                            .actions(Arrays.asList(
                                                                                    "ssm:PutParameter",
                                                                                    "ssm:GetParametersByPath",
                                                                                    "ssm:GetParameters",
                                                                                    "ssm:GetParameter",
                                                                                    "ssm:GetParameterHistory",
                                                                                    "ssm:DescribeParameters"
                                                                            ))
                                                                            .effect(Effect.ALLOW)
                                                                            // TODO: Restrict
                                                                            .resources(Arrays.asList("*"))
                                                                            .build());

        Role role = new Role(stack, "PutSsmParameterRole",
                RoleProps
                        .builder()
                        .assumedBy(new AccountPrincipal(config.getPipeline().getDeploy().getAccount()))
                        .roleName(
                                Label.builder()
                                     .resource("PutSsmParameter")
                                     .namespace(config.getName())
                                     .stage(stage.name())
                                     .build().toString())
                        .build());


        role.addToPolicy(statement);

        stackOutputService.addOutput(stack, stage, CfnOutput.Builder
                .create(stack, "PublishSsmRoleArn")
                .description("IAM Role that can publish to SSM")
                .value(role.getRoleArn())
                .exportName(stackNameToCfnOutput(Stack.of(stack).getStackName(), "PublishSsmRoleArn"))
                .build());
    }

    private void addCreateEc2ImageRole(Stack stack, Environment stage) {
        // TODO: Refactor into repository
        PolicyStatement statement = new PolicyStatement(PolicyStatementProps.builder()
                                                                            .actions(Arrays.asList(
                                                                                    "autoscaling:*",
                                                                                    "ec2:*"
                                                                            ))
                                                                            .effect(Effect.ALLOW)
                                                                            .resources(Arrays.asList("*"))
                                                                            .build());

        Role role = new Role(stack, "CreateEc2ImageRole",
                RoleProps
                        .builder()
                        .assumedBy(new AccountPrincipal(config.getPipeline().getDeploy().getAccount()))
                        .roleName(
                                Label.builder()
                                     .resource("CreateEc2Image")
                                     .namespace(config.getName())
                                     .stage(stage.name())
                                     .build().toString())
                        .build());


        role.addToPolicy(statement);

        stackOutputService.addOutput(stack, stage, CfnOutput.Builder
                .create(stack, "CreateEc2ImageRoleRoleArn")
                .description("IAM Role that can create Ec2 images")
                .value(role.getRoleArn())
                .exportName(stackNameToCfnOutput(Stack.of(stack).getStackName(), "CreateEc2ImageRoleArn"))
                .build());
    }

    private String stackNameToCfnOutput(String stackName, String param) {
        return String.format("%s-%s", stackName, param).replaceAll("/", "-");
    }

}
