package com.example.demo.config;

import org.apache.commons.text.WordUtils;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Builder
@Log4j2
public class Label {

    private String stage;
    private String namespace;
    private String resource;

    public String toString() {
        return String.format("%s%s%s", WordUtils.capitalizeFully(namespace), WordUtils.capitalizeFully(stage),
                WordUtils.capitalizeFully(resource));
    }
}
