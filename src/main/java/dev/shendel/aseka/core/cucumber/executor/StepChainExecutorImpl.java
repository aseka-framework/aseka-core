package dev.shendel.aseka.core.cucumber.executor;

import dev.shendel.aseka.core.service.RetryExecutor;
import dev.shendel.aseka.core.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public final class StepChainExecutorImpl implements StepChainExecutor {

    private final List<RunnableStep> steps = new ArrayList<>();
    private int retrySeconds = 1;
    private boolean enabled = false;

    @Override
    public void enable(Integer retrySeconds) {
        if (!enabled) {
            resetState();
        }
        this.retrySeconds = retrySeconds;
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void addStep(RunnableStep step) {
        Validator.checkThat(enabled, "Can't add step to chain. Executor disabled");
        steps.add(step);
    }

    @Override
    public void execute() {
        enabled = false;
        RetryExecutor
                .of(retrySeconds)
                .execute(() -> steps.forEach(Runnable::run));
        log.info("Executed {} steps.", steps.size());
    }

    private void resetState() {
        steps.clear();
    }

    @Override
    public void clean() {
        Validator.checkThat(!enabled, "Step chain not executed");
    }

}
