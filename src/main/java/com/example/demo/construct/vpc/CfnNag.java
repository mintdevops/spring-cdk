package com.example.demo.construct.vpc;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CfnNag {
    List<CfnNagRule> rules_to_suppress;
}
