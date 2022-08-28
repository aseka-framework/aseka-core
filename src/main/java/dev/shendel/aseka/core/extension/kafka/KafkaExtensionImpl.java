package dev.shendel.aseka.core.extension.kafka;

import dev.shendel.aseka.core.configuration.KafkaProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaExtensionImpl implements KafkaExtension {

    private final KafkaProperties kafkaProperties;
    private final Map<String, KafkaTopicClient> topicClients = new HashMap<>();
    private final Map<String, Deque<KafkaMessage>> uncommittedMessages = new HashMap<>();

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
                    uncommittedMessages.put(topicName, new ArrayDeque<>());
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
        uncommittedMessages.get(topicName).clear();
    }

    @Override
    public KafkaMessage receiveMessage(String topicName) {
        return pollMessage(topicName);
    }

    private KafkaMessage pollMessage(String topicName) {
        Deque<KafkaMessage> topicUncommittedMessages = uncommittedMessages.get(topicName);
        KafkaMessage messageFromKafka = getTopicClient(topicName).receiveMessage();

        KafkaMessage message = messageFromKafka != null ? messageFromKafka : topicUncommittedMessages.pollFirst();

        if (message != null) topicUncommittedMessages.offerLast(message);
        return message;
    }

    @Override
    public void commitMessage(KafkaMessage message) {
        uncommittedMessages.forEach((topic, messages) -> {
            boolean removed = messages.remove(message);
            if (removed) {
                log.info("Kafka message committed: {}", message.getUid());
            }
        });
    }

    @Override
    public void clean() {
        uncommittedMessages.forEach((topic, messageDeque) -> messageDeque.clear());
    }

    private KafkaTopicClient getTopicClient(String topicName) {
        return Optional.ofNullable(topicClients.get(topicName))
                .orElseThrow(() -> new AsekaException("Topic '{}' doesn't have settings", topicName));
    }

}
