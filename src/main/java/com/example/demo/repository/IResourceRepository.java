package com.example.demo.repository;

import java.util.List;

import com.example.demo.config.Environment;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.IConstruct;

public interface IResourceRepository<R extends IConstruct, C extends IResourceConfig> {

    R create(Construct scope, String namespace, Environment stage, C conf);

    R lookup(Construct scope, String stackName, LookupType lookupType);

    List<CfnOutput> export(Construct scope, R resource);

}
