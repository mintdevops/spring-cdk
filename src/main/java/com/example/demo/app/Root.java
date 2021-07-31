package com.example.demo.app;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.TagManager;
import com.example.demo.svc.TaggingService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Tags;

@Component
@Log4j2
@Getter
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Root {

    private final AppConfig config;
    private final TaggingService taggingService;
    private final App rootScope = new App();
    private Map<String, String> tags = new HashMap<>();

    public void synth() {
        log.debug("App:synth");

        // for (Map.Entry<String, String> entry : tags.entrySet()) {
        //     Tags.of(rootScope).add(entry.getKey(), entry.getValue());
        // }

        taggingService.addApplicationTags(rootScope);

        rootScope.synth();
    }

    // @PostConstruct
    // private void addTags() {
    //     log.debug("addTags");

    //     tags = TagManager.fullyQualifiedTags(config.getTagNamespace(), "app",
    //             config.getTags());



    // }

}
