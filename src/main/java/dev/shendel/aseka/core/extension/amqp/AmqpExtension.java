package dev.shendel.aseka.core.extension.amqp;

import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.api.Extension;

public interface AmqpExtension extends Extension {

    void sendToQueue(String queueName, String body);

    void purgeQueue(String queueName);

    MqMessage receiveMessage(String queueName);

}
