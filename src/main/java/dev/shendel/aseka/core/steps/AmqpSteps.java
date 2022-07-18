package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.cucumber.executor.RetryableStep;
import dev.shendel.aseka.core.cucumber.type.InterpolatedString;
import dev.shendel.aseka.core.extension.amqp.AmqpExtension;
import dev.shendel.aseka.core.extension.amqp.model.MqMessage;
import dev.shendel.aseka.core.matcher.global.GlobalMatcherFactory;
import dev.shendel.aseka.core.service.FileManager;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static dev.shendel.aseka.core.util.Asserts.assertThat;

@Data
@Slf4j
@RequiredArgsConstructor
public class AmqpSteps {

    private final FileManager fileManager;
    private final AmqpExtension extension;
    private final GlobalMatcherFactory globalMatcherFactory;

    @When("send to queue {interpolated_string} message:")
    public void sendMessage(String queueName, InterpolatedString message) {
        extension.sendToQueue(queueName, message.get());
    }

    @When("send to queue {interpolated_string} message {file_path}")
    public void sendMessage(String queueName, String messagePath) {
        String message = fileManager.readFileAsString(messagePath);
        Allure.addAttachment("message", message);
        extension.sendToQueue(queueName, message);
    }

    @When("purge queue {interpolated_string}")
    public void purgeQueue(String queueName) {
        extension.purgeQueue(queueName);
    }

    @RetryableStep(defaultRetrySeconds = "3")
    @When("check that message in queue {interpolated_string} is {file_path}")
    public void checkMessage(String queueName, String messagePath) {
        String expectedMessage = fileManager.readFileAsString(messagePath);
        Allure.addAttachment("expectedMessage", expectedMessage);
        checkMessageInternal(queueName, expectedMessage);
    }

    @RetryableStep(defaultRetrySeconds = "3")
    @When("check that message in queue {interpolated_string} is:")
    public void checkMessage(String queueName, InterpolatedString expectedMessage) {
        checkMessageInternal(queueName, expectedMessage.get());
    }

    private void checkMessageInternal(String queueName, String expectedMessage) {
        MqMessage actualMessage = extension.receiveMessage(queueName);
        log.info("Checking actual message: {}", actualMessage);
        assertThat(actualMessage.getBody(), globalMatcherFactory.create(expectedMessage));
    }

}
