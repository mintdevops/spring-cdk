package com.example.demo.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.App;

@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Root {

    private final AppConfig config;
    private final App rootScope = new App();;

    @PostConstruct
    public void provision() {
        log.debug(config.toString());
    }

    public void synth() {
        log.debug("synth");

        rootScope.synth();
    }

}
