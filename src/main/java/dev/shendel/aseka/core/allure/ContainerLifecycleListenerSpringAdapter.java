package dev.shendel.aseka.core.allure;

import io.qameta.allure.listener.ContainerLifecycleListener;
import io.qameta.allure.model.TestResultContainer;

import java.util.Collection;

public class ContainerLifecycleListenerSpringAdapter implements ContainerLifecycleListener {

    private Collection<ContainerLifecycleListener> getDelegates() {
        return SpringContext.getBeans(ContainerLifecycleListener.class).values();
    }

    @Override
    public void beforeContainerStart(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.beforeContainerStart(container));
    }

    @Override
    public void afterContainerStart(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.afterContainerStart(container));
    }

    @Override
    public void beforeContainerUpdate(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.beforeContainerUpdate(container));
    }

    @Override
    public void afterContainerUpdate(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.afterContainerUpdate(container));
    }

    @Override
    public void beforeContainerStop(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.beforeContainerStop(container));
    }

    @Override
    public void afterContainerStop(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.afterContainerStop(container));
    }

    @Override
    public void beforeContainerWrite(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.beforeContainerWrite(container));
    }

    @Override
    public void afterContainerWrite(TestResultContainer container) {
        getDelegates().forEach(delegate -> delegate.afterContainerWrite(container));
    }

}
