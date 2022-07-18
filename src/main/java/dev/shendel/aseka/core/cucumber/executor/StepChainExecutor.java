package dev.shendel.aseka.core.cucumber.executor;

import dev.shendel.aseka.core.api.Cleanable;

public interface StepChainExecutor extends Cleanable {


    void enable(Integer retrySeconds);

    boolean isEnabled();

    void addStep(RunnableStep step);

    void execute();

}
