package com.example.demo.construct.imagebuilder;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Builder(access = AccessLevel.PUBLIC)
@Data
public class Distribution {

    String region;
    AmiDistributionConfiguration amiDistributionConfiguration;

}
