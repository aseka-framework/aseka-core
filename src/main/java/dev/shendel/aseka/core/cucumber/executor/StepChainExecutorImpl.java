package dev.shendel.aseka.core.cucumber.executor;

import dev.shendel.aseka.core.service.RetryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.shendel.aseka.core.util.Validator.checkThat;


@Slf4j
@Component
public final class StepChainExecutorImpl implements StepChainExecutor {

    private final List<RunnableStep> steps = new ArrayList<>();
    private int maxRetrySeconds = 1;
    private boolean active = false;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void startCollectingSteps() {
        if (!active) {
            steps.clear();
        }
        active = true;
    }

    @Override
    public void executeCollectedSteps() {
        active = false;
        RetryExecutor
                .of(maxRetrySeconds)
                .retryExceptions(AssertionError.class)
                .execute(() -> steps.forEach(Runnable::run));

        log.info("{} steps was executed", steps.size());
        steps.clear();
    }

    @Override
    public void setMaxRetrySeconds(Integer maxRetrySeconds) {
        this.maxRetrySeconds = maxRetrySeconds;
    }

    @Override
    public void addStep(RunnableStep step) {
        checkThat(active, "executor is not active");
        steps.add(step);
    }

    @Override
    public void clean() {
        checkThat(!active, "Step chain not executed");
    }

}
