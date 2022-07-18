package dev.shendel.aseka.core.extension.amqp;

import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.extension.amqp.model.AmqpBrokerType;

public interface AmqpAdapter {

    boolean supports(AmqpBrokerType type);

    void sendToQueue(String queueName, String body);

    void purgeQueue(String queueName);

    MqMessage receiveMessage(String queueName);

    void prepareBrokers();

    void destroyBrokers();

}
