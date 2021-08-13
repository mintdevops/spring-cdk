
package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.StackFactory;
import com.example.demo.repository.ImageBuilderRepository;
import com.example.demo.repository.ImmutableServerRepository;
import com.example.demo.repository.VpcRepository;
import com.example.demo.service.TaggingService;
import com.example.demo.service.cloudformation.AppStackService;
import com.example.demo.service.cloudformation.ImageStackService;
import com.example.demo.service.cloudformation.StackOutputService;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import au.com.origin.snapshots.serializers.DeterministicJacksonSnapshotSerializer;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Stack;

@ExtendWith(SnapshotExtension.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableConfigurationProperties(value = AppConfig.class)
@ContextConfiguration(classes = {
        AppStackService.class,
        StackFactory.class,
        ImageBuilderRepository.class,
        ImmutableServerRepository.class,
        VpcRepository.class,
        TaggingService.class,
        StackOutputService.class
})
@ActiveProfiles("ec2-workload")
public class Ec2WorkloadStackTest {

    @Autowired
    private AppStackService appStackService;

    @Test
    public void devStack(Expect expect) {
        App app = new App();

        Stack s = appStackService.provision(app, "Dev", Environment.DEV);

        expect.serializer(DeterministicJacksonSnapshotSerializer.class).toMatchSnapshot(
                app.synth()
                   .getStackArtifact(s.getArtifactId())
                   .getTemplate());
    }

    @Test
    public void testStack(Expect expect) {
        App app = new App();

        Stack s = appStackService.provision(app, "Test", Environment.TEST);

        expect.serializer(DeterministicJacksonSnapshotSerializer.class).toMatchSnapshot(
                app.synth()
                   .getStackArtifact(s.getArtifactId())
                   .getTemplate());
    }


    @Test
    public void prodStack(Expect expect) {
        App app = new App();

        Stack s = appStackService.provision(app, "Prod", Environment.PROD);

        expect.serializer(DeterministicJacksonSnapshotSerializer.class).toMatchSnapshot(
                app.synth()
                   .getStackArtifact(s.getArtifactId())
                   .getTemplate());
    }

}
