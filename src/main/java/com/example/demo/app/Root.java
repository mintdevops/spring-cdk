package com.example.demo.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;
import com.example.demo.service.PipelineStackService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.App;

/**
 * The type Root.
 */
@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Root {

    private final AppConfig config;
    private final App rootScope = new App();
    private final PipelineStackService pipelineStackService;

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        log.debug(config.toString());
    }

    /**
     * Synth.
     */
    public void synth() {
        log.debug("synth");

        pipelineStackService.provision(rootScope, "", Environment.CICD);

        rootScope.synth();
    }

}
