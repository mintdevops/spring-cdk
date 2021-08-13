package com.example.demo.construct.imagebuilder.dist;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmiDistributionConfiguration {

    String name;
    String description;
    Map<String, String> amiTags;
    LaunchPermission launchPermissionConfiguration;
}
