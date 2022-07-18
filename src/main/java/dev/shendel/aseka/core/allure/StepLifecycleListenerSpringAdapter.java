package dev.shendel.aseka.core.allure;

import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.StepResult;

import java.util.Collection;

public class StepLifecycleListenerSpringAdapter implements StepLifecycleListener {

    private Collection<StepLifecycleListener> getDelegates() {
        return SpringContext.getBeans(StepLifecycleListener.class).values();
    }

    @Override
    public void beforeStepStart(StepResult result) {
        getDelegates().forEach(delegate -> delegate.beforeStepStart(result));
    }

    @Override
    public void afterStepStart(StepResult result) {
        getDelegates().forEach(delegate -> delegate.afterStepStart(result));
    }

    @Override
    public void beforeStepUpdate(StepResult result) {
        getDelegates().forEach(delegate -> delegate.beforeStepUpdate(result));
    }

    @Override
    public void afterStepUpdate(StepResult result) {
        getDelegates().forEach(delegate -> delegate.afterStepUpdate(result));
    }

    @Override
    public void beforeStepStop(StepResult result) {
        getDelegates().forEach(delegate -> delegate.beforeStepStop(result));
    }

    @Override
    public void afterStepStop(StepResult result) {
        getDelegates().forEach(delegate -> delegate.afterStepStop(result));
    }
}
