package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.api.Cleanable;
import dev.shendel.aseka.core.context.ContextVariables;
import dev.shendel.aseka.core.cucumber.executor.StepChainExecutor;
import dev.shendel.aseka.core.cucumber.executor.StepExecutorIgnore;
import dev.shendel.aseka.core.cucumber.parser.MatcherFactory;
import dev.shendel.aseka.core.cucumber.type.Triple;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.matcher.AsekaMatcher;
import dev.shendel.aseka.core.matcher.IsEqualFile;
import dev.shendel.aseka.core.matcher.object.ObjectMatcherType;
import dev.shendel.aseka.core.matcher.object.ObjectMatcherFactory;
import dev.shendel.aseka.core.service.FileManager;
import dev.shendel.aseka.core.service.StringInterpolator;
import dev.shendel.aseka.core.util.Asserts;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static dev.shendel.aseka.core.util.Asserts.assertThat;
import static dev.shendel.aseka.core.util.Validator.checkThat;

@Slf4j
@RequiredArgsConstructor
public class CommonSteps {

    private final List<Cleanable> cleanableBins;
    private final StepChainExecutor stepChainExecutor;
    private final StringInterpolator stringInterpolator;
    private final ContextVariables contextVariables;
    private final ObjectMatcherFactory objectMatcherFactory;
    private final FileManager fileManager;

    @When("set variables:")
    public void setVariables(Map<String, String> variables) {
        for (Map.Entry<String, String> row : variables.entrySet()) {
            String key = stringInterpolator.interpolate(row.getKey());
            String value = stringInterpolator.interpolate(row.getValue());
            contextVariables.set(key, value);
        }
    }

    @Then("check variables:")
    @SuppressWarnings("unchecked")
    public void checkVariables(List<Triple> rows) {
        for (Triple row : rows) {
            String variableName = row.getFirst();
            String matcherName = row.getSecond();
            String expectedValue = row.getThird();
            String actualValue = contextVariables.get(variableName);

            AsekaMatcher asekaMatcher = AsekaMatcher.getBy(matcherName);
            checkThat(asekaMatcher.isStringMatcher(), "Only string matchers supported.");

            org.hamcrest.Matcher mather = MatcherFactory.create(asekaMatcher, expectedValue);

            assertThat(
                    actualValue,
                    mather,
                    "Variable '{}' don't matches condition: `{} {}`",
                    variableName,
                    matcherName,
                    expectedValue
            );
        }
    }

    @When("set object matcher {object_matcher}")
    public void setObjectMatcher(ObjectMatcherType objectMatcherType) {
        objectMatcherFactory.setObjectMatcherType(objectMatcherType);
    }

    @SneakyThrows
    @When("wait {int} ms")
    public void wait(Integer ms) {
        Thread.sleep(ms);
    }

    @SneakyThrows
    @When("wait {int} s")
    public void waitSeconds(Integer seconds) {
        Thread.sleep(seconds * 1000);
    }

    @SneakyThrows
    @When("wait {interpolated_string} s")
    public void waitSeconds(String stringSeconds) {
        int seconds;
        try {
            seconds = Integer.parseInt(stringSeconds);
        } catch (Exception exception) {
            throw new AsekaException("{} must be a number", stringSeconds);
        }
        Thread.sleep(seconds * 1000L);
    }

    @When("clean context")
    public void cleanContext() {
        cleanableBins.forEach(Cleanable::clean);
    }

    @When("clean variables")
    public void resetVariablesToDefault() {
        contextVariables.clean();
    }

    @When("set variables by regexp:")
    public void variableTakenWithRegexToNewVariable(List<Triple> rows) {
        for (Triple row : rows) {
            String newVariableName = row.getFirst();
            String regex = row.getSecond();
            String oldVariableName = row.getThird();

            String content = contextVariables.get(oldVariableName);
            java.util.regex.Matcher matcher = Pattern.compile(regex).matcher(content);
            Asserts.assertThat(matcher.find(), "Can't find variable by pattern '{}' in '{}'", regex, content);
            String result = matcher.group();

            contextVariables.set(newVariableName, result);
        }
    }

    @StepExecutorIgnore
    @When("---- retry {int} second(s)")
    public void startRetryStepsChainV2(int maxRetrySeconds) {
        stepChainExecutor.setMaxRetrySeconds(maxRetrySeconds);
        stepChainExecutor.startCollectingSteps();
    }

    @StepExecutorIgnore
    @When("---- end retryable block")
    public void stopStepsChainV2() {
        stepChainExecutor.executeCollectedSteps();
    }

    @Deprecated
    @StepExecutorIgnore
    @When("---- start retryable steps block. Retry no longer than {int} second(s).")
    public void startRetryStepsChain(int maxRetrySeconds) {
        stepChainExecutor.setMaxRetrySeconds(maxRetrySeconds);
        stepChainExecutor.startCollectingSteps();
    }

    @Deprecated
    @StepExecutorIgnore
    @When("---- end retryable steps block")
    public void stopStepsChain() {
        stepChainExecutor.executeCollectedSteps();
    }

    @Then("files {file_path} and {file_path} is equal")
    public void checkFilesEquality(String actualFilePath, String expectedFilePath) {
        File actual = fileManager.getFile(actualFilePath);
        File expected = fileManager.getFile(expectedFilePath);
        Asserts.assertThat(actual, IsEqualFile.isEqualFile(expected), "Files are not equal");
    }

    @When("log all variables")
    public void logVariables() {
        log.info("Context contain variables: \n {}", contextVariables.getAll());
    }

    @When("---- {interpolated_string}")
    public void description(String description) {
        log.info("Description: {}", description);
    }

}
