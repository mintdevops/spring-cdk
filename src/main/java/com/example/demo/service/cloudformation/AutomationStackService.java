package com.example.demo.service.cloudformation;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.Label;
import com.example.demo.core.StackType;
import com.example.demo.core.StackFactory;
import com.example.demo.service.AbstractInfrastructureService;
import com.example.demo.service.TaggingService;
import com.example.demo.service.cloudformation.StackOutputService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

/**
 * A service to provision Cloudformation stack to manage automation between pipeline stages.
 *
 * The CDK bootstrap template creates a few roles to support cross-account deployment in CodePipeline however there
 * is a need to provision additional roles that are specific to the types of workloads being managed by the pipeline.
 * These resources need to be deployed before any stages which deploy and validate infrastructure are executed.
 *
 * The caveat here is that these resources are referred to by their physical resource ID's instead of their logical
 * resource ID's as their Arn's need to be predictable.
 *
 * FIXME: Use logical Id's as this is considered bad practice in the general sense but is acceptable for this use case
 */
@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AutomationStackService extends AbstractInfrastructureService {

    public static final StackType QUALIFIER = StackType.PIPELINE;

    private final AppConfig config;
    private final StackFactory stackFactory;
    private final TaggingService taggingService;
    private final StackOutputService stackOutputService;

    private final String publishAmiIdRoleName = "PublishAmiId";
    private final String publishAmiIdRoleOutputName = "PublishAmiIdRoleArn";
    private final String createEc2ImageRoleName = "CreateEc2Image";
    private final String createEc2ImageRoleOutputName = "CreateEc2ImageRoleArn";

    @Override
    public Stack provision(Construct scope, String namespace, Environment stage) {
        Stack stack = stackFactory.create(scope, namespace, stage);

        addPublishAmiIdRole(stack, namespace, stage);
        addCreateEc2ImageRole(stack, namespace, stage);

        taggingService.addTags(stack, config.getEnv().get(stage).getTags(), QUALIFIER.name());

        return stack;
    }

    /**
     * Creates a role that can be assumed by CodePipeline that can call put-parameter in the target account.
     *
     * NB: The principal needs sts:AssumeRole permissions for the role Arn.
     *
     * @param stack
     * @param stage
     */
    private void addPublishAmiIdRole(Stack stack, String namespace, Environment stage){
        // TODO: Refactor into role repository
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
                                                                            .resources(Collections.singletonList("*"))
                                                                            .build());

        Role role = new Role(stack, "PublishAmiIdRole",
                RoleProps
                        .builder()
                        .assumedBy(new AccountPrincipal(config.getPipeline().getDeploy().getAccount()))
                        .roleName(Label.builder()
                                       .namespace(Stack.of(stack).getStackName())
                                       //.stage(stage)
                                       .resource(publishAmiIdRoleName)
                                       .build()
                                       .toPhysicalId())
                        .build());


        role.addToPolicy(statement);

        stackOutputService.addOutput(stack, stage, CfnOutput.Builder
                .create(stack, "PublishAmiIdRoleArn")
                .description("IAM role that can publish to SSM")
                .value(role.getRoleArn())
                .exportName(
                        Label.builder()
                             .namespace(Stack.of(stack).getStackName())
                             .resource(publishAmiIdRoleOutputName)
                             .build()
                             .toCfnExport())
                .build());
    }

    /**
     * Creates a role that can be assumed by CodePipeline that can call create-image in the target account.
     *
     * NB: The principal needs sts:AssumeRole permissions for the role Arn.
     *
     * @param stack
     * @param stage
     */
    private void addCreateEc2ImageRole(Stack stack, String namespace, Environment stage) {
        // TODO: Refactor into role repository
        PolicyStatement statement = new PolicyStatement(PolicyStatementProps.builder()
                                                                            .actions(Arrays.asList(
                                                                                    "autoscaling:*",
                                                                                    "ec2:*"
                                                                            ))
                                                                            .effect(Effect.ALLOW)
                                                                            // TODO: Restrict
                                                                            .resources(Collections.singletonList("*"))
                                                                            .build());

        Role role = new Role(stack, "CreateEc2ImageRole",
                RoleProps
                        .builder()
                        .assumedBy(new AccountPrincipal(config.getPipeline().getDeploy().getAccount()))
                        .roleName(
                                Label.builder()
                                     .namespace(Stack.of(stack).getStackName())
                                     //.stage(stage)
                                     .resource(createEc2ImageRoleName)
                                     .build()
                                     .toPhysicalId())
                        .build());


        role.addToPolicy(statement);

        stackOutputService.addOutput(stack, stage, CfnOutput.Builder
                .create(stack, "CreateEc2ImageRoleRoleArn")
                .description("IAM eole that can create EC2 images")
                .value(role.getRoleArn())
                .exportName(
                        Label.builder()
                             .namespace(Stack.of(stack).getStackName())
                             .resource(createEc2ImageRoleOutputName)
                             .build()
                             .toCfnExport())
                .build());
    }

}
