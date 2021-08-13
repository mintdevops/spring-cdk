package com.example.demo.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.service.TaggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.Stack;

/**
 * A factory for creating CDK stacks consistently.
 *
 * Stacks are a unit of deployment within CDK and are environment-agnostic by default. The client determines whether or
 * not the stack needs to be configured within a specific environment context.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StackFactory {

    /**
     * Apply deployment-level tags
     */
    private final TaggingService taggingService;

    /**
     * Create an environment-agnostic stack.
     *
     * @param scope    the scope
     * @param namespace the namespace
     * @return the stack
     */
    public Stack create(Construct scope, String namespace, com.example.demo.core.Environment stage) {
        return create(scope, namespace, stage,"", "");
    }

    /**
     * Create stack for a specific AWS environment.
     *
     * @param scope    the scope
     * @param namespace the namespace
     * @param account   the account
     * @param region    the region
     * @return the stack
     */
    public Stack create(Construct scope, String namespace, com.example.demo.core.Environment stage, String account,
                        String region) {
        Stack stack = Stack.Builder.create(scope, namespace)
                                   .stackName(
                                           Label.builder()
                                                .namespace(namespace)
                                                .stage(stage)
                                                .build()
                                                .toStackName()
                                   )
                                   .env(Environment.builder()
                                                   .account(!account.isEmpty() ?  account : System.getenv(
                                                           "CDK_DEFAULT_ACCOUNT"))
                                                   .region(!region.isEmpty() ? region : System.getenv("CDK_DEFAULT_REGION"))
                                                   .build()
                                   )
                                   .build();

        taggingService.addApplicationTags(stack);

        return stack;
    }

}
