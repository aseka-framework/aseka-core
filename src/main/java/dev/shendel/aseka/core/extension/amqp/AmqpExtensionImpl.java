package dev.shendel.aseka.core.extension.amqp;

import dev.shendel.aseka.core.configuration.AmqpProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.extension.amqp.model.Broker;
import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.util.Validator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class AmqpExtensionImpl implements AmqpExtension {

    private final AmqpProperties properties;
    private final List<AmqpAdapter> adapters;
    private final Map<String, Deque<MqMessage>> uncommittedMessages = new HashMap<>();

    @Override
    @SneakyThrows
    public void init() {
        if (properties.isEnabled()) {
            log.info("Initializing AMQP extension...");
            validateProperties();
            adapters.forEach(AmqpAdapter::prepareBrokers);
            properties.getAllQueues()
                    .forEach(queue -> uncommittedMessages.put(queue.getName(), new ArrayDeque<>()));
        }
    }

    @Override
    public void sendToQueue(String queueName, MessageProperties props, String body) {
        getAdapterFor(queueName).sendToQueue(queueName, props, body);
    }

    @Override
    public void purgeQueue(String queueName) {
        getAdapterFor(queueName).purgeQueue(queueName);
        uncommittedMessages.get(queueName).clear();
    }

    @Override
    public MqMessage receiveMessage(String queueName) {
        return pollMessage(queueName);
    }

    @Override
    public void commitMessage(MqMessage message) {
        uncommittedMessages.forEach((topic, messageDeque) -> {
            boolean removed = messageDeque.remove(message);
            if (removed) {
                log.info("Mq message committed: {}", message.getUid());
            }
        });
    }

    private MqMessage pollMessage(String queueName) {
        Deque<MqMessage> queueUncommittedMessages = uncommittedMessages.get(queueName);
        MqMessage messageFromMq = getAdapterFor(queueName).receiveMessage(queueName);

        MqMessage message = messageFromMq != null ? messageFromMq : queueUncommittedMessages.pollFirst();

        if (message != null) queueUncommittedMessages.offerLast(message);
        return message;
    }

    private AmqpAdapter getAdapterFor(String queueName) {
        Broker broker = properties.getBrokerBy(queueName);
        return adapters.stream()
                .filter(adapter -> adapter.supports(broker.getType()))
                .findFirst()
                .orElseThrow(() -> new AsekaException("Can't find MQ adapter for '{}'", broker.getType()));
    }

    @Override
    public void destroy() {
        adapters.forEach(AmqpAdapter::destroyBrokers);
    }

    @Override
    public void clean() {
        uncommittedMessages.forEach((queue, messages) -> messages.clear());
    }

    //TODO check all properties is valid
    private void validateProperties() {
        properties.getBrokers()
                .forEach(broker -> {
                    Validator.checkThatNotBlank(broker.getName(), "Invalid mq properties. Broker name is blank");
                    Validator.checkThatNotNull(broker.getType(), "Invalid mq properties. Broker type is null");
                });
        properties.getBrokers()
                .stream()
                .flatMap(broker -> broker.getQueues().stream())
                .forEach(queue -> {
                    Validator.checkThatNotBlank(queue.getName(), "Invalid mq properties. Queue name is blank");
                });
    }

}
