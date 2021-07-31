package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TagManager {

    public static Map<String, String> fullyQualifiedTags(String namespace, String qualifier, Map<String,
            String> tags) {
        Map<String, String> mutatedTags = new HashMap<>();

        log.debug("Adding tags {}", String.join(",", tags.keySet()));

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            // TODO: Implement sanitizeString() method compatible with CF
            String tagName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);;
            String tagValue = entry.getValue().replaceAll("^\"|\"$", "");

            String fqdn = String.format("%s%s",
                    (namespace == null || namespace.isEmpty() ? "" : namespace.replaceAll("^\"|\"$", "") + "."),
                    (qualifier == null || qualifier.isEmpty() ? "" : qualifier.replaceAll("^\"|\"$", "") + "/"));

            mutatedTags.put(String.format("%s%s", fqdn.trim(), tagName), tagValue);
        }

        return mutatedTags;
    }

}
