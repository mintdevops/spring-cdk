package com.example.demo.app;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Construct;

@Component
@Log4j2
@Getter
public class Root {

    App rootScope;

    @PostConstruct
    public void provision() {
        log.debug("App:synth");

        rootScope = new App();
    }

    public void synth() {
        rootScope.synth();
    }

}
