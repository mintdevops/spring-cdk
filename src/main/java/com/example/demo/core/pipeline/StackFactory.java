package com.example.demo.core.pipeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.service.TaggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.Stack;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StackFactory {

    private final TaggingService taggingService;

    public Stack create(Construct parent, String namespace) {
        log.debug("create");

        Stack stack = Stack.Builder.create(parent, namespace)
                            .env(Environment.builder()
                                            .account(System.getenv(
                                                    "CDK_DEFAULT_ACCOUNT"))
                                            .region(System.getenv("CDK_DEFAULT_REGION"))
                                            .build()
                            )
                            .build();

        taggingService.addApplicationTags(stack);

        return stack;
    }

    public Stack create(Construct parent, String namespace, String account, String region) {
        log.debug("create");

        Stack stack = Stack.Builder.create(parent, namespace)
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
