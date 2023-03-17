package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.cucumber.executor.RetryableStep;
import dev.shendel.aseka.core.cucumber.type.InterpolatedString;
import dev.shendel.aseka.core.cucumber.type.Pair;
import dev.shendel.aseka.core.extension.amqp.AmqpExtension;
import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.matcher.object.ObjectMatcherFactory;
import dev.shendel.aseka.core.service.FileManager;
import dev.shendel.aseka.core.service.RetryExecutor;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.Matchers.not;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.shendel.aseka.core.util.Asserts.assertThat;

@Slf4j
@RequiredArgsConstructor
public class AmqpSteps {

    private final FileManager fileManager;
    private final AmqpExtension extension;
    private final ObjectMatcherFactory objectMatcherFactory;

    @Getter @Setter
    private MessageProperties messageProperties;

    @PostConstruct
    public void initBeforeEveryCase() {
        messageProperties = new MessageProperties();
    }

    @When("set mq message content-type {interpolated_string}")
    public void setContentType(String contentType) {
        messageProperties.setContentType(contentType);
    }

    @When("set mq message headers:")
    public void setMessageHeaders(List<Pair> headers) {
        Map<String, String> headersMap = headers.stream()
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        messageProperties.setHeaders(headersMap);
    }

    @When("send to queue {interpolated_string} message:")
    public void sendMessage(String queueName, InterpolatedString message) {
        extension.sendToQueue(queueName, messageProperties, message.get());
    }

    @When("send to queue {interpolated_string} message {file_path}")
    public void sendMessage(String queueName, String messagePath) {
        String message = fileManager.readFileAsString(messagePath);
        Allure.addAttachment("message", message);
        extension.sendToQueue(queueName, messageProperties, message);
    }

    @When("purge queue {interpolated_string}")
    public void purgeQueue(String queueName) {
        extension.purgeQueue(queueName);
    }

    @When("check that queue {interpolated_string} is empty for {int} sec")
    public void checkQueueIsEmpty(String queueName, Integer seconds) {
        RetryExecutor.of(seconds)
                .retryUntilTheEnd()
                .execute(() -> {
                    MqMessage actualMessage = extension.receiveMessage(queueName);
                    assertThat(actualMessage == null, "Queue is not empty: {}", queueName);
                });
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that message in queue {interpolated_string} is {file_path}")
    public void checkMessage(String queueName, String messagePath) {
        String expectedMessage = fileManager.readFileAsString(messagePath);
        checkMessageInternal(queueName, expectedMessage);
    }

    @When("check that message not found in queue {interpolated_string} for {int} sec. message: {file_path}")
    public void checkNotExistMessage(String queueName, Integer seconds, String messagePath) {
        String expectedMessage = fileManager.readFileAsString(messagePath);
        checkNotExistMessageInternal(queueName, seconds, expectedMessage);
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that message in queue {interpolated_string} is:")
    public void checkMessage(String queueName, InterpolatedString expectedMessage) {
        checkMessageInternal(queueName, expectedMessage.get());
    }

    @When("check that message not found in queue {interpolated_string} for {int} sec. message:")
    public void checkNotExistMessage(String queueName, Integer seconds, InterpolatedString expectedMessage) {
        checkNotExistMessageInternal(queueName, seconds, expectedMessage.get());
    }

    @SuppressWarnings("ConstantConditions")
    private void checkMessageInternal(String queueName, String expectedMessage) {
        MqMessage actualMessage = extension.receiveMessage(queueName);
        Allure.addAttachment("expectedMessage", expectedMessage);
        Allure.addAttachment(
                "actualMessage",
                Optional.ofNullable(actualMessage).map(MqMessage::getBody).orElse("null")
        );
        log.info("Checking actual message: {}", actualMessage);
        assertThat(actualMessage != null, "Don't have messages in queue {}", queueName);
        assertThat(actualMessage.getBody(), objectMatcherFactory.isEqualObject(expectedMessage));
        extension.commitMessage(actualMessage);
    }

    @SuppressWarnings("ConstantConditions")
    private void checkNotExistMessageInternal(String queueName,Integer seconds, String expectedMessage) {
        RetryExecutor.of(seconds)
                .retryUntilTheEnd()
                .execute(() ->
                {
                    MqMessage actualMessage = extension.receiveMessage(queueName);
                    Allure.addAttachment("expectedMessage", expectedMessage);
                    Allure.addAttachment("actualMessage",
                            Optional.ofNullable(actualMessage).map(MqMessage::getBody).orElse("null"));

                    log.info("Checking actual message: {}", actualMessage);
                    if (Objects.nonNull(actualMessage)) {
                        assertThat(actualMessage.getBody(), not(objectMatcherFactory.isEqualObject(expectedMessage)),
                                "Unexpected message found in queue: {}", queueName);
                    }
                    extension.commitMessage(actualMessage);
                });
    }
}
