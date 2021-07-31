package com.example.demo.construct.imagebuilder;


import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageBuilderConfig {

    List<String> regions;
    List<String> accounts;
    String vpcId;
    List<String> availabilityZones;
    String subnetId;
    String imageName;

}
