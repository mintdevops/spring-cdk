package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.config.Environment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Tags;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class TaggingService {

    private final AppConfig conf;

    public static Map<String, String> fullyQualifiedTags(String namespace, String qualifier, Map<String, String> tags) {
        Map<String, String> mutated = new HashMap<>();

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String tagName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);;
            String tagValue = entry.getValue().replaceAll("^\"|\"$", "");

            String fqdn = String.format("%s%s",
                    (namespace == null || namespace.isEmpty() ? "" : namespace.toLowerCase().replaceAll("^\"|\"$",
                            "") + "."),
                    (qualifier == null || qualifier.isEmpty() ? "" : qualifier.toLowerCase().replaceAll("^\"|\"$",
                            "") + "/"));

            mutated.put(String.format("%s%s", fqdn.trim(), tagName), tagValue);
        }

        return mutated;
    }

    public void addApplicationTags(Construct scope) {
        log.debug("addApplicationTags");
        
        resolveTags(scope, conf.getTags(), "app");
    }

    public void addEnvironmentTags(Construct scope, Environment env, String qualifier) {
        log.debug("addEnvironmentTags");

        Map<String, String> envTags = new HashMap<>();
        envTags.put("Environment", env.name());

        resolveTags(scope, envTags, qualifier);
    }

    public void addTags(Construct scope, Map<String, String> tags, String qualifier) {
        log.debug("addTags");

        resolveTags(scope, tags, qualifier);
    }

    private void resolveTags(Construct scope, Map<String, String> tags, String qualifier) {
        Map<String,String> mutated = TaggingService.fullyQualifiedTags(conf.getTagNamespace(), qualifier, tags);

        log.debug("Adding tags {} to {} ", String.join(",", mutated.keySet()), scope.getNode().getId());

        for (Map.Entry<String, String> entry : mutated.entrySet()) {
            Tags.of(scope).add(entry.getKey(), entry.getValue());
        }
    }
}