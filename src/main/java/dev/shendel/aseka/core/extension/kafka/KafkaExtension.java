package dev.shendel.aseka.core.extension.kafka;

import dev.shendel.aseka.core.api.Extension;
import org.jetbrains.annotations.NotNull;

public interface KafkaExtension extends Extension {

    @Override
    void init();

    @Override
    void destroy();

    void sendToTopic(String topicName, String body);

    void resetOffsetToEnd(String topicName);

    //TODO think about returning null
    @NotNull KafkaMessage receiveMessage(String topicName);

}
