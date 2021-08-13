package com.example.demo.core;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;

import lombok.Builder;
import lombok.Data;

/**
 * A utility class to enforce consistent naming conventions across CDK applications resources
 */
@Data
@Builder
public class Label {

    private String namespace;
    private Environment stage;
    private String resource;

    public String toSsmParameterName() {
        return "/".concat(toStream().collect(Collectors.joining(
                "/")));
    }

    public String toStackName() {
        return toStream().collect(Collectors.joining("-"));
    }

    public String toLogicalId(boolean withStage) {
        return toStream().map(WordUtils::capitalizeFully).collect(Collectors.joining(""));
    }

    public String toLogicalId() {
        return toLogicalId(false);
    }

    public String toCfnExport() {
        return toStream().collect(Collectors.joining("-"));
    }

    public String toPhysicalId() {
        return toStream().collect(Collectors.joining("-"));
    }

    public String toEnvVar() {
        return toStream().map(String::toUpperCase).collect(Collectors.joining("_"));
    }

    private Stream<String> toStream() {
        return Stream.of(namespace, stage == null ? null : stage.name(), resource).filter(Objects::nonNull);
    }

}
