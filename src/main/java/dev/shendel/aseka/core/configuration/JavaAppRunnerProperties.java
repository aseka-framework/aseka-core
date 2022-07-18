package dev.shendel.aseka.core.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.java-app-runner")
public class JavaAppRunnerProperties {

    private boolean enabled = false;
    private boolean appLoggingEnabled = true;
    private String javaPath;
    private String appPath;
    private Map<String, String> environment = new HashMap<>();
    private Map<String, String> appProperties = new HashMap<>();
    private String healthCheckUrl;
    private int healthCheckTimeoutSeconds;

}
