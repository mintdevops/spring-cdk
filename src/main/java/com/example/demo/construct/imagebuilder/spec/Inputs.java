package com.example.demo.construct.imagebuilder.spec;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
class InputSerializer extends StdSerializer<Inputs> {

    public InputSerializer() {
        this(null);
    }

    public InputSerializer(Class<Inputs> t) {
        super(t);
    }

    @Override
    public void serialize(
            Inputs value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        log.debug(value.toString());

        if (value.getSource() != null) {
            log.debug("Serializing array");

            jgen.writeStartArray();
            jgen.writeStartObject();
            jgen.writeStringField("source", value.getSource());
            jgen.writeStringField("destination", value.getDestination());
            jgen.writeEndObject();
            jgen.writeEndArray();
        } else {
            log.debug("Serializing dict");

            jgen.writeStartObject();
            if (value.getCommands() != null) {
                jgen.writeArrayFieldStart("commands");
                for (String cmd : value.getCommands()) {
                    jgen.writeString(cmd);
                }
                jgen.writeEndArray();

            }
            if (value.getPath() != null) {
                jgen.writeStringField("path", value.getPath());
            }
            if (value.getArguments() != null) {
                jgen.writeArrayFieldStart("arguments");
                for (String arg : value.getArguments()) {
                    jgen.writeString(arg);
                }
                jgen.writeEndArray();

            }
            jgen.writeEndObject();
        }
    }
}

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(using = InputSerializer.class)
public class Inputs {

    private List<String> commands;
    private String path;
    private List<String> arguments;
    private String source;
    private String destination;

}
