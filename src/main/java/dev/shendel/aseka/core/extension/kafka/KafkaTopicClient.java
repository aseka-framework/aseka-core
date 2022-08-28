package dev.shendel.aseka.core.extension.kafka;

import dev.shendel.aseka.core.exception.AsekaException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;

import java.io.Closeable;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Getter
public class KafkaTopicClient implements Closeable {

    private static final Integer DEFAULT_POLL_TIMEOUT = 140;

    private final String topicName;
    private final KafkaProducer<Object, String> producer;
    private final KafkaConsumer<Object, String> consumer;

    public KafkaTopicClient(String topicName, Map<String, String> properties) {
        this.topicName = topicName;

        producer = new KafkaProducer<>(buildProducerProps(properties));
        consumer = new KafkaConsumer<>(buildConsumerProps(properties));

        log.info("Subscribing to topic: {}", topicName);
        consumer.subscribe(newArrayList(topicName));
        waitUntilAssigned(topicName);
        resetOffsetToEnd();
    }

    private Properties buildProducerProps(Map<String, String> properties) {
        Properties resultProperties = new Properties();

        resultProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        resultProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        resultProperties.putAll(properties);
        return resultProperties;
    }

    private Properties buildConsumerProps(Map<String, String> properties) {
        Properties resultProperties = new Properties();
        resultProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        resultProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        resultProperties.put("enable.auto.commit", "true");
        resultProperties.put("auto.commit.interval.ms", "100");
        resultProperties.put("max.poll.interval.ms", "300000");
        resultProperties.put("heartbeat.interval.ms", "200");
        resultProperties.put("auto.offset.reset", "earliest");
        resultProperties.put("group.id", "tests-" + topicName);

        resultProperties.putAll(properties);

        resultProperties.put("max.poll.records", "1");
        return resultProperties;
    }

    private void waitUntilAssigned(String topicName) {
        StopWatch stopWatch = StopWatch.createStarted();
        //https://www.smilecdr.com/our-blog/kafka-integration-testing-partition-assignment
        //use this because we need block until all partitions assigned
        consumer.poll(DEFAULT_POLL_TIMEOUT);
        Set<TopicPartition> topicPartitions = consumer.assignment();
        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
        for (TopicPartition partition : topicPartitions) {
            long position = consumer.position(partition, Duration.ofMillis(DEFAULT_POLL_TIMEOUT));
            log.info(
                    "Assigned to topic in {} ms. topicName:{}, partition:{}, offset: {}, position: {}",
                    stopWatch.getTime(TimeUnit.MILLISECONDS),
                    topicName,
                    partition.partition(),
                    endOffsets.get(partition),
                    position
            );
        }
    }

    public void send(String body) {
        producer.send(new ProducerRecord<>(topicName, null, null, null, body, null));
    }

    public void resetOffsetToEnd() {
        boolean resetFinished = false;
        int skippedMessages = 0;
        StopWatch stopWatch = StopWatch.createStarted();
        while (!resetFinished) {
            ConsumerRecords<Object, String> records = consumer.poll(Duration.ofMillis(DEFAULT_POLL_TIMEOUT));
            skippedMessages = skippedMessages + records.count();
            resetFinished = records.isEmpty();
        }
        log.info(
                "Topic '{}' offset reset in {} ms. Skipped messages count: {}",
                topicName,
                stopWatch.getTime(TimeUnit.MILLISECONDS),
                skippedMessages
        );
    }

    @Override
    public void close() {
        consumer.close();
        producer.close();
    }

    public KafkaMessage receiveMessage() {
        ConsumerRecords<Object, String> records = consumer.poll(Duration.ofMillis(DEFAULT_POLL_TIMEOUT));
        if (records.isEmpty()) {
            return null;
        } else if (records.count() == 1) {
            ConsumerRecord<Object, String> record = records.iterator().next();
            return KafkaMessage.of(record.value(), record.offset(), getHeaders(record));
        } else {
            throw new AsekaException("Wrong fetched messages count. Please change kafka consumer settings.");
        }
    }

    private Map<String, String> getHeaders(ConsumerRecord<Object, String> record) {
        return Stream.of(record.headers().toArray())
                .collect(Collectors.toMap(Header::key, header -> new String(header.value())));
    }

}
