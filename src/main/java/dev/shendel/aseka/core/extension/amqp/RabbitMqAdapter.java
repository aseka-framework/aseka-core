package dev.shendel.aseka.core.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dev.shendel.aseka.core.configuration.AmqpProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.extension.amqp.model.AmqpBrokerType;
import dev.shendel.aseka.core.extension.amqp.model.Broker;
import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.extension.amqp.model.Queue;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.rabbitmq.client.BuiltinExchangeType.DIRECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqAdapter implements AmqpAdapter {

    private final AmqpProperties properties;
    private final Map<Broker, Connection> connections = new HashMap<>();
    private final Map<Queue, Channel> channels = new HashMap<>();

    @Override
    public boolean supports(AmqpBrokerType type) {
        return type == AmqpBrokerType.RABBIT_MQ;
    }

    @Override
    public void sendToQueue(String queueName, MessageProperties props, String body) {
        Queue queue = properties.getQueueByName(queueName);
        Channel channel = channels.get(queue);
        try {
            channel.basicPublish(
                    queue.getExchange(),
                    queue.getName(),
                    convertToRabbitProps(props),
                    body.getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            throw new AsekaException("Error sending message to {}", exception, queueName);
        }
    }

    private AMQP.BasicProperties convertToRabbitProps(MessageProperties props) {
        if (props.isEmpty()) {
            return null;
        } else {
            return new AMQP.BasicProperties(
                    props.getContentType(),
                    props.getContentEncoding(),
                    new HashMap<String, Object>() {
                        {
                            putAll(props.getHeaders());
                        }
                    },
                    props.getDeliveryMode(),
                    props.getPriority(),
                    props.getCorrelationId(),
                    props.getReplyTo(),
                    props.getExpiration(),
                    props.getMessageId(),
                    props.getTimestamp(),
                    props.getType(),
                    props.getUserId(),
                    props.getAppId(),
                    props.getClusterId()
            );
        }
    }

    @Override
    public void purgeQueue(String queueName) {
        Channel channel = channels.get(properties.getQueueByName(queueName));
        try {
            channel.queuePurge(queueName);
        } catch (IOException exception) {
            throw new AsekaException("Error purge '{}'", exception, queueName);
        }
    }

    @Override
    public MqMessage receiveMessage(String queueName) {
        Channel channel = channels.get(properties.getQueueByName(queueName));
        try {
            return Optional.ofNullable(channel.basicGet(queueName, true))
                    .map(response -> MqMessage.of(new String(response.getBody()), new HashMap<>()))
                    .orElse(MqMessage.empty());
        } catch (IOException exception) {
            throw new AsekaException("Error receive message '{}'", exception, queueName);
        }
    }

    @Override
    public void prepareBrokers() {
        getRabbitBrokers().forEach(this::createConnection);
        getRabbitQueues().forEach(
                queue -> {
                    createChannel(queue);
                    declareQueue(queue);
                    //TODO add declaring exchange and bind
                    //declareExchange(queue);
                    //declareBind(queue);
                }
        );
    }

    private void createConnection(Broker broker) {
        try {
            log.info("Creating connection to broker '{}'", broker.getName());
            Connection connection = new ConnectionFactory().load(broker.getProperties(), null).newConnection();
            connections.put(broker, connection);
        } catch (IOException | TimeoutException e) {
            throw new AsekaException("Error connecting to broker `{}`", e, broker.getName());
        }
    }

    private void createChannel(Queue queue) {
        try {
            log.info("Creating channel for queue '{}'", queue.getName());
            Broker broker = properties.getBrokerBy(queue);
            Connection connection = connections.get(broker);
            Channel channel = connection.createChannel();
            channels.put(queue, channel);
        } catch (IOException e) {
            throw new AsekaException("Error creating channel for queue '{}'", e, queue.getName());
        }
    }

    @SneakyThrows
    private void declareQueue(Queue queue) {
        channels.get(queue)
                .queueDeclare(queue.getName(), true, false, false, new HashMap<>());
    }

    @SneakyThrows
    private void declareExchange(Queue queue) {
        channels.get(queue)
                .exchangeDeclare(queue.getExchange(), DIRECT);
    }

    @SneakyThrows
    private void declareBind(Queue queue) {
        channels.get(queue)
                .queueBind(queue.getName(), queue.getExchange(), queue.getName());
    }

    private List<Broker> getRabbitBrokers() {
        return properties.getBrokers()
                .stream()
                .filter(broker -> broker.getType() == AmqpBrokerType.RABBIT_MQ)
                .collect(Collectors.toList());
    }

    private List<Queue> getRabbitQueues() {
        return getRabbitBrokers().stream()
                .flatMap(broker -> broker.getQueues().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void destroyBrokers() {
        getRabbitBrokers().forEach(this::closeConnection);
    }

    private void closeConnection(Broker broker) {
        try {
            connections.get(broker).close();
        } catch (IOException exception) {
            log.warn("Error closing connection to RabbitMQ", exception);
        }
    }

}
