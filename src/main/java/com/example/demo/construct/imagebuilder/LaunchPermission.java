package com.example.demo.construct.imagebuilder;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LaunchPermission {

    List<String> userIds;
}
