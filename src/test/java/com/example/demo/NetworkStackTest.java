
package com.example.demo;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.demo.config.AppConfig;
import com.example.demo.core.Environment;
import com.example.demo.core.StackFactory;
import com.example.demo.repository.NatGatewayRepository;
import com.example.demo.repository.VpcRepository;
import com.example.demo.service.TaggingService;
import com.example.demo.service.cloudformation.NetworkStackService;
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
        NetworkStackService.class,
        StackFactory.class,
        VpcRepository.class,
        NatGatewayRepository.class,
        TaggingService.class,
        StackOutputService.class
})
@ActiveProfiles("network")
public class NetworkStackTest {

    @Autowired
    private NetworkStackService networkStackService;

    @Test
    public void devStack(Expect expect) {
        App app = new App();

        Stack s = networkStackService.provision(app, "Dev", Environment.DEV);

        expect.serializer(DeterministicJacksonSnapshotSerializer.class).toMatchSnapshot(
                app.synth()
                   .getStackArtifact(s.getArtifactId())
                   .getTemplate());
    }

    @Test
    public void testStack(Expect expect) {
        App app = new App();

        Stack s = networkStackService.provision(app, "Test", Environment.TEST);

        expect.serializer(DeterministicJacksonSnapshotSerializer.class).toMatchSnapshot(
                app.synth()
                   .getStackArtifact(s.getArtifactId())
                   .getTemplate());
    }

    @Test
    public void prodStack(Expect expect) {
        System.out.println(networkStackService);

        App app = new App();

        Stack s = networkStackService.provision(app, "Prod", Environment.PROD);

        expect.serializer(DeterministicJacksonSnapshotSerializer.class).toMatchSnapshot(
                app.synth()
                   .getStackArtifact(s.getArtifactId())
                   .getTemplate());
    }

}
