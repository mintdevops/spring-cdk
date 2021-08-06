package com.example.demo.config;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CfnNagRule {

    String id;
    String reason;

}
