package dev.shendel.aseka.core.cucumber.executor;

import dev.shendel.aseka.core.api.Cleanable;

public interface StepChainExecutor extends Cleanable {

    boolean isActive();

    void startCollectingSteps();

    void executeCollectedSteps();

    void setMaxRetrySeconds(Integer maxRetrySeconds);

    void addStep(RunnableStep step);

}
