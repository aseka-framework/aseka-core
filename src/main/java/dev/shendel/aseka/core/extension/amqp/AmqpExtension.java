package dev.shendel.aseka.core.extension.amqp;

import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.api.Extension;

import javax.annotation.Nullable;

public interface AmqpExtension extends Extension {

    void sendToQueue(String queueName, MessageProperties props, String body);

    void purgeQueue(String queueName);

    @Nullable
    MqMessage receiveMessage(String queueName);

    void commitMessage(MqMessage message);

    @Override
    void clean();

}
