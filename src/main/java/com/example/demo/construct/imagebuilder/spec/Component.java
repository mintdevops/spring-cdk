package com.example.demo.construct.imagebuilder.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Log4j2
public class Component {

    static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    }

    private String name;
    private String description;
    private String schemaVersion;
    private List<Phases> phases;

    public String toDocument() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}
