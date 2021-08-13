package com.example.demo.construct.vpc;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CfnNagRule {

    String id;
    String reason;

}
