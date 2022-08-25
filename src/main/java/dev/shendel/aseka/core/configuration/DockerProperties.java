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
    private String containersNamePrefix = "aseka";
    private List<String> composeFiles;
    private Integer healthcheckWaitTimeoutMinutes = 3;
    private Integer waitAfterStartSeconds = null;

}
