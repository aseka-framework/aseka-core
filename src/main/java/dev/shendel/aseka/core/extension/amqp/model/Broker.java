package dev.shendel.aseka.core.extension.amqp.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Broker {

    private String name;

    /**
     * {@link com.rabbitmq.client.ConnectionFactoryConfigurator}
     */
    private final Map<String, String> properties = new HashMap<>();
    private AmqpBrokerType type;

    private List<Queue> queues;
    private List<Exchange> exchanges;
    private List<Binding> bindings;

}
