package dev.shendel.aseka.core.configuration.restassured;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.http")
public class HttpProperties {

    private Integer timeout = 60000;

    private boolean logsEnabled = true;

    private String baseUrl;

}
