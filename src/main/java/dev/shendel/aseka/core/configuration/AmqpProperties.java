package dev.shendel.aseka.core.configuration;

import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.extension.amqp.model.Broker;
import dev.shendel.aseka.core.extension.amqp.model.Queue;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.amqp")
public class AmqpProperties {

    private boolean enabled = false;
    private List<Broker> brokers = new ArrayList<>();

    public Broker getBrokerBy(String queueName) {
        Queue queue = getQueueByName(queueName);
        return getBrokerBy(queue);
    }

    public Broker getBrokerBy(Queue queue) {
        return brokers.stream()
                .filter(broker -> broker.getQueues()
                        .stream()
                        .anyMatch(innerQueue -> innerQueue.equals(queue)))
                .findFirst()
                .orElseThrow(() -> new AsekaException("TODO")); //TODO exception comment
    }

    public Queue getQueueByName(String name) {
        return brokers.stream()
                .flatMap(broker -> broker.getQueues().stream())
                .filter(queue -> queue.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AsekaException("Invalid mq properties. Can't find queue with name: `{}`", name));
    }

}
