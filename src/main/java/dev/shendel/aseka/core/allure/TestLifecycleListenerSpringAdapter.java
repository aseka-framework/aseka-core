package dev.shendel.aseka.core.allure;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collection;

@Data
@Configurable
public class TestLifecycleListenerSpringAdapter implements TestLifecycleListener {

    private Collection<TestLifecycleListener> getDelegates() {
        return SpringContext.getBeans(TestLifecycleListener.class).values();
    }

    @Override
    public void beforeTestSchedule(TestResult result) {
        getDelegates().forEach(delegate -> delegate.beforeTestSchedule(result));
    }

    @Override
    public void afterTestSchedule(TestResult result) {
        getDelegates().forEach(delegate -> delegate.afterTestSchedule(result));
    }

    @Override
    public void beforeTestUpdate(TestResult result) {
        getDelegates().forEach(delegate -> delegate.beforeTestUpdate(result));
    }

    @Override
    public void afterTestUpdate(TestResult result) {
        getDelegates().forEach(delegate -> delegate.afterTestUpdate(result));
    }

    @Override
    public void beforeTestStart(TestResult result) {
        getDelegates().forEach(delegate -> delegate.beforeTestStart(result));
    }

    @Override
    public void afterTestStart(TestResult result) {
        getDelegates().forEach(delegate -> delegate.afterTestStart(result));
    }

    @Override
    public void beforeTestStop(TestResult result) {
        getDelegates().forEach(delegate -> delegate.beforeTestStop(result));
    }

    @Override
    public void afterTestStop(TestResult result) {
        getDelegates().forEach(delegate -> delegate.afterTestStop(result));
    }

    @Override
    public void beforeTestWrite(TestResult result) {
        getDelegates().forEach(delegate -> delegate.beforeTestWrite(result));
    }

    @Override
    public void afterTestWrite(TestResult result) {
        getDelegates().forEach(delegate -> delegate.afterTestWrite(result));
    }


}
