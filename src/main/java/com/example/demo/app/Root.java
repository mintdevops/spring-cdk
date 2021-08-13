package com.example.demo.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.service.cloudformation.PipelineStackService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.App;

/**
 * CDK Application Root. Provisions a pipeline stack which manages the underlying infrastructure
 */
@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Root {

    private final AppConfig config;
    private final App rootScope = new App();
    private final PipelineStackService pipelineStackService;

    @PostConstruct
    public void init() {
        log.debug(config.toString());

        // TODO: Support other sources for current git version
        String resourceName = "git.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try {
            try(InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
                props.load(resourceStream);

                config.setVersion(props.getProperty("git.commit.id.abbrev"));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Generate the CloudFormation templates to cdk.out
     */
    public void synth() {
        log.debug("synth");

        // TODO: Add demo stack to application root (not managed by pipeline)
        pipelineStackService.provision(rootScope, "", Environment.CICD);

        rootScope.synth();
    }

}
