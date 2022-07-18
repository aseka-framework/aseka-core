package dev.shendel.aseka.core.extension.kafka;

import dev.shendel.aseka.core.configuration.KafkaProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaExtensionImpl implements KafkaExtension {

    private final Map<String, KafkaTopicClient> topicClients = new HashMap<>();
    private final KafkaProperties kafkaProperties;

    @Override
    public void init() {
        if (kafkaProperties.isEnabled()) {
            log.info("Initializing Kafka extension...");
            for (Broker broker : kafkaProperties.getBrokers()) {
                for (Topic topic : broker.getTopics()) {
                    String topicName = topic.getName();
                    Map<String, String> mergedProperties = getMergedProperties(
                            broker.getProperties(),
                            topic.getProperties()
                    );
                    topicClients.put(topicName, new KafkaTopicClient(topicName, mergedProperties));
                }
            }
        }
    }

    private Map<String, String> getMergedProperties(Map<String, String> brokerProps, Map<String, String> topicProps) {
        HashMap<String, String> mergedProperties = new HashMap<>();
        mergedProperties.putAll(brokerProps);
        mergedProperties.putAll(topicProps);
        return mergedProperties;
    }

    @Override
    public void destroy() {
        topicClients.values().forEach(KafkaTopicClient::close);
    }

    @Override
    public void sendToTopic(String topicName, String body) {
        getTopicClient(topicName).send(body);
    }

    @Override
    public void resetOffsetToEnd(String topicName) {
        getTopicClient(topicName).resetOffsetToEnd();
    }

    @Override
    public @NotNull KafkaMessage receiveMessage(String topicName) {
        return getTopicClient(topicName).receiveMessage();
    }

    private KafkaTopicClient getTopicClient(String topicName) {
        return Optional.ofNullable(topicClients.get(topicName))
                .orElseThrow(() -> new AsekaException("Topic '{}' doesn't have settings", topicName));
    }

}
