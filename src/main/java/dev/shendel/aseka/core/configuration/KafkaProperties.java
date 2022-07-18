package dev.shendel.aseka.core.configuration;

import dev.shendel.aseka.core.extension.kafka.Broker;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.kafka")
public class KafkaProperties {

    private boolean enabled = false;
    private List<Broker> brokers = new ArrayList<>();

}
