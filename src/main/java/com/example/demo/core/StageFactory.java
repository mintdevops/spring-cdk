package com.example.demo.core;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.Label;
import com.example.demo.core.StackType;
import com.example.demo.service.IInfrastructureService;
import com.example.demo.service.TaggingService;
import com.example.demo.service.cloudformation.AppStackService;
import com.example.demo.service.cloudformation.ImageStackService;
import com.example.demo.service.cloudformation.NetworkStackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stage;

/**
 * Factory for producing pipeline stages.
 * <p>
 * Pipeline stages are synthesized as separate Cloud Assemblies in cdk.out and uploaded to the CDK staging bucket by
 * the pipeline. These assets are then consumed downstream by CloudFormation actions.
 * <p>
 * Stages are not deployed, so are not environment-aware from a deployment perspective (AWS environment), but are
 * environment-aware from a configuration perspective.
 */
@Component
@Log4j2
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StageFactory {

    private final static String RESOURCE_NAME = "Stage";

    private final AppConfig conf;
    private final TaggingService taggingService;

    /**
     * Provision a stack in to a stage.
     *
     * @param parent
     * @param namespace
     * @param stage
     * @param service
     * @return
     */
    public Stage create(Construct parent, String namespace, Environment stage, IInfrastructureService service) {
        String envAccount = conf.getEnv().get(stage).getDeploy().getAccount();
        String envRegion = conf.getEnv().get(stage).getDeploy().getRegion();
        String pipelineAccount = conf.getPipeline().getDeploy().getAccount();
        String pipelineRegion = conf.getPipeline().getDeploy().getRegion();

        // Figure out where we're deploying the stacks defined in this stage to in order of precedence:
        // 1. Stage configuration
        // 2. Pipeline configuration
        // 3. Environment configuration
        software.amazon.awscdk.core.Environment env;
        if (!envAccount.isEmpty() && !envRegion.isEmpty()) {
            env = software.amazon.awscdk.core.Environment.builder().account(envAccount).region(envRegion).build();
        }
        else if (!pipelineAccount.isEmpty() && !pipelineRegion.isEmpty()) {
            env = software.amazon.awscdk.core.Environment.builder().account(pipelineAccount).region(pipelineRegion).build();
        }
        else {
            env = software.amazon.awscdk.core.Environment.builder().account(System.getenv(
                    "CDK_DEFAULT_ACCOUNT")).region(System.getenv("CDK_DEFAULT_REGION")).build();
        }

        Stage pipelineStage = Stage.Builder.create(parent,
                Label.builder()
                     // NB: Stage names impact the name of cloud assembles and the output of `cdk ls`
                     .namespace(namespace)
                     .stage(stage)
                     .resource(RESOURCE_NAME)
                     .build()
                     .toLogicalId(true)) // Avoid naming conflict in pipeline stack
               .env(env)
               .build();

        log.debug("Adding stack {} to stage {}", service.getClass().getCanonicalName(), pipelineStage.getStageName());

        // NB: Namespace will influence the stack name
        // NB: Stage names impact the name of cloud assembles and the output of `cdk ls`
        service.provision(pipelineStage, String.format("%s%s", conf.getApplicationName(), namespace), stage);

        taggingService.addEnvironmentTags(pipelineStage, stage, conf.getPipeline().getStack());

        return pipelineStage;
    }

}