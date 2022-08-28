package dev.shendel.aseka.core.extension.kafka;

import dev.shendel.aseka.core.api.Extension;

import javax.annotation.Nullable;

public interface KafkaExtension extends Extension {

    @Override
    void init();

    @Override
    void destroy();

    void sendToTopic(String topicName, String body);

    void resetOffsetToEnd(String topicName);

    @Nullable
    KafkaMessage receiveMessage(String topicName);

    void commitMessage(KafkaMessage message);

    @Override
    void clean();

}
