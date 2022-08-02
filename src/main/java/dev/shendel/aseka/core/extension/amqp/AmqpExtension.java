package dev.shendel.aseka.core.extension.amqp;

import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.api.Extension;

public interface AmqpExtension extends Extension {

    void sendToQueue(String queueName, MessageProperties props, String body);

    void purgeQueue(String queueName);

    MqMessage receiveMessage(String queueName);

}
