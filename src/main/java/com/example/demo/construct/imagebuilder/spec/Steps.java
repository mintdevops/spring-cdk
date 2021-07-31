package com.example.demo.construct.imagebuilder.spec;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Steps {

    private String name;
    private String action;
    private Inputs inputs;
}
