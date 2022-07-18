package dev.shendel.aseka.core.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.wiremock")
public class WiremockProperties {

    private boolean enabled = false;
    private boolean embeddedServerEnabled = false;
    private String host = "localhost";
    private Integer port = 8484;
    private String defaultMocksPath;

}
