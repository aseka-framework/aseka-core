package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.cucumber.executor.RetryableStep;
import dev.shendel.aseka.core.cucumber.type.InterpolatedString;
import dev.shendel.aseka.core.extension.kafka.KafkaExtension;
import dev.shendel.aseka.core.extension.kafka.KafkaMessage;
import dev.shendel.aseka.core.matcher.object.ObjectMatcherFactory;
import dev.shendel.aseka.core.service.FileManager;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static dev.shendel.aseka.core.util.Asserts.assertThat;

@Slf4j
@RequiredArgsConstructor
public class KafkaSteps {

    private final FileManager fileManager;
    private final KafkaExtension extension;
    private final ObjectMatcherFactory objectMatcherFactory;

    @When("send to topic {interpolated_string} message:")
    public void sendMessage(String topicName, InterpolatedString message) {
        extension.sendToTopic(topicName, message.get());
    }

    @When("send to topic {interpolated_string} message {file_path}")
    public void sendMessage(String topicName, String messagePath) {
        String message = fileManager.readFileAsString(messagePath);
        Allure.addAttachment("message", message);
        extension.sendToTopic(topicName, message);
    }

    @When("reset offset to end for topic {interpolated_string}")
    public void resetOffsetToEnd(String topicName) {
        extension.resetOffsetToEnd(topicName);
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that message in topic {interpolated_string} is {file_path}")
    public void checkMessage(String topicName, String messagePath) {
        String expectedMessage = fileManager.readFileAsString(messagePath);
        Allure.addAttachment("expectedMessage", expectedMessage);
        checkMessageInternal(topicName, expectedMessage);
    }

    @RetryableStep(defaultRetrySeconds = "2")
    @When("check that message in topic {interpolated_string} is:")
    public void checkMessage(String topicName, InterpolatedString expectedMessage) {
        checkMessageInternal(topicName, expectedMessage.get());
    }

    private void checkMessageInternal(String topicName, String expectedMessage) {
        KafkaMessage actualMessage = extension.receiveMessage(topicName);
        log.info("Checking actual message: {}", actualMessage);
        assertThat(actualMessage.getBody(), objectMatcherFactory.create(expectedMessage));
    }

}
