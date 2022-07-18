package dev.shendel.aseka.core.allure;

import io.qameta.allure.listener.FixtureLifecycleListener;
import io.qameta.allure.model.FixtureResult;

import java.util.Collection;

public class FixtureLifecycleListenerSpringAdapter implements FixtureLifecycleListener {

    private Collection<FixtureLifecycleListener> getDelegates() {
        return SpringContext.getBeans(FixtureLifecycleListener.class).values();
    }

    @Override
    public void beforeFixtureStart(FixtureResult result) {
        getDelegates().forEach(delegate -> delegate.beforeFixtureStart(result));
    }

    @Override
    public void afterFixtureStart(FixtureResult result) {
        getDelegates().forEach(delegate -> delegate.afterFixtureStart(result));
    }

    @Override
    public void beforeFixtureUpdate(FixtureResult result) {
        getDelegates().forEach(delegate -> delegate.beforeFixtureUpdate(result));
    }

    @Override
    public void afterFixtureUpdate(FixtureResult result) {
        getDelegates().forEach(delegate -> delegate.afterFixtureUpdate(result));
    }

    @Override
    public void beforeFixtureStop(FixtureResult result) {
        getDelegates().forEach(delegate -> delegate.beforeFixtureStop(result));
    }

    @Override
    public void afterFixtureStop(FixtureResult result) {
        getDelegates().forEach(delegate -> delegate.afterFixtureStop(result));
    }
}
