package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.cucumber.executor.RetryableStep;
import dev.shendel.aseka.core.cucumber.type.InterpolatedString;
import dev.shendel.aseka.core.cucumber.type.Pair;
import dev.shendel.aseka.core.extension.amqp.AmqpExtension;
import dev.shendel.aseka.core.extension.amqp.model.MessageProperties;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.matcher.object.ObjectMatcherFactory;
import dev.shendel.aseka.core.service.FileManager;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.shendel.aseka.core.util.Asserts.assertThat;
import static dev.shendel.aseka.core.util.Asserts.assertTrue;
import static dev.shendel.aseka.core.matcher.NotMatcher.not;

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

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check queue {interpolated_string} is empty")
    public void checkQueueIsEmpty(String queueName) {
        MqMessage actualMessage = extension.receiveMessage(queueName);
        assertTrue(actualMessage == null, "Queue is not empty: " + getReplace(queueName));
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that message in queue {interpolated_string} is {file_path}")
    public void checkMessage(String queueName, String messagePath) {
        String expectedMessage = fileManager.readFileAsString(messagePath);
        checkMessageInternal(queueName, expectedMessage);
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that not exist message in queue {interpolated_string} is {file_path}")
    public void checkNotExistMessage(String queueName, String messagePath) {
        String expectedMessage = fileManager.readFileAsString(messagePath);
        checkNotExistMessageInternal(queueName, expectedMessage);
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that message in queue {interpolated_string} is:")
    public void checkMessage(String queueName, InterpolatedString expectedMessage) {
        checkMessageInternal(queueName, expectedMessage.get());
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that not exist message in queue {interpolated_string} is:")
    public void checkNotExistMessage(String queueName, InterpolatedString expectedMessage) {
        checkNotExistMessageInternal(queueName, expectedMessage.get());
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
    private void checkNotExistMessageInternal(String queueName, String expectedMessage) {
        MqMessage actualMessage = extension.receiveMessage(queueName);
        Allure.addAttachment("expectedMessage", expectedMessage);
        Allure.addAttachment("actualMessage",
                Optional.ofNullable(actualMessage).map(MqMessage::getBody).orElse("null")
        );
        log.info("Checking actual message: {}", actualMessage);
        if(actualMessage == null) {
            assertTrue(actualMessage == null, "Queue is not empty: " + getReplace(queueName));
        } else {
            assertThat(actualMessage.getBody(), not(objectMatcherFactory.create(expectedMessage)),
                    "Unexpected message found in queue: " + getReplace(queueName));
        }
        extension.commitMessage(actualMessage);
    }

    private static String getReplace(String queueName) {
        return queueName.replace("$", "\\$");
    }

}
