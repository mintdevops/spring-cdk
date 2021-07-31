package com.example.demo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;

import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;

@Component
@Log4j2
public class VpcFactory {

    @Autowired
    AppConfig config;

    private String name = "Vpc";

    public Construct create(Construct parent) {
        // Just return an empty construct for now the implementation details arent important here

        log.debug("VpcFactory:create");
        log.debug(config);

        return new Construct(parent, name);
    }

    // standard setters and getters
}