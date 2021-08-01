package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class StageConfig {

    List<IStack> stacks = new ArrayList<>();

}
