package com.example.demo.construct.pipeline;

import com.example.demo.config.GithubConfig;
import com.example.demo.construct.ISpec;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PipelineSpec implements ISpec {

    private String pipelineName;
    private GithubConfig github;

}
