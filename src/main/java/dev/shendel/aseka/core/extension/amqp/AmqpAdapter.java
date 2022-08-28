package dev.shendel.aseka.core.extension.amqp;

import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.extension.amqp.model.AmqpBrokerType;

import javax.annotation.Nullable;

public interface AmqpAdapter {

    boolean supports(AmqpBrokerType type);

    void sendToQueue(String queueName, MessageProperties props, String body);

    void purgeQueue(String queueName);

    @Nullable
    MqMessage receiveMessage(String queueName);

    void prepareBrokers();

    void destroyBrokers();

}
