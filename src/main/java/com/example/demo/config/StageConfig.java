package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import software.amazon.awscdk.core.Stack;

@Data
public class StageConfig {

    List<IStack> stacks = new ArrayList<>();

}
