package com.example.demo.stack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Tags;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Getter
@Setter
public class TaggingService {

    private final AppConfig conf;
    private String namespace;
    private Map<String, String> appTags = new HashMap<>();

    public static Map<String, String> fullyQualifiedTags(String namespace, String qualifier, Map<String,
    String> tags) {
        Map<String, String> mutatedTags = new HashMap<>();

        log.debug("Adding tags {}", String.join(",", tags.keySet()));

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String tagName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);;
            String tagValue = entry.getValue().replaceAll("^\"|\"$", "");

            String fqdn = String.format("%s%s",
                    (namespace == null || namespace.isEmpty() ? "" : namespace.toLowerCase().replaceAll("^\"|\"$",
                            "") + "."),
                    (qualifier == null || qualifier.isEmpty() ? "" : qualifier.toLowerCase().replaceAll("^\"|\"$",
                            "") + "/"));

            mutatedTags.put(String.format("%s%s", fqdn.trim(), tagName), tagValue);
        }

        return mutatedTags;
    }

    @PostConstruct
    public void loadUserTags() {
        log.debug("loadUserTags");

        this.namespace = conf.getTagNamespace();
        this.appTags = conf.getTags();
    }

    public void addTags(Construct scope, Map<String, String> tags, String qualifier) {
        log.debug("addTags");

        Map<String,String> merged = Stream.concat(this.appTags.entrySet().stream(), tags.entrySet().stream()).collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String,String> mutated = TaggingService.fullyQualifiedTags(namespace, qualifier, merged);

        log.debug(mutated);

        for (Map.Entry<String, String> entry : mutated.entrySet()) {
            Tags.of(scope).add(entry.getKey(), entry.getValue());
        }
    }
}