package com.example.demo.construct.imagebuilder.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Phases {

    private String name;
    private List<Steps> steps;
}
