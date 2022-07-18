package dev.shendel.aseka.core.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.docker")
public class DockerProperties {

    private boolean enabled = false;
    private List<String> composeFiles;

    //TODO write own testcontainer with default wating healthcheck .waitingFor("rabbitmq", Wait.forHealthcheck());
    // based on https://github.com/testcontainers/testcontainers-java
    private long postStartDelay = 2000;

}
