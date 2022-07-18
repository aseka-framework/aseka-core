package dev.shendel.aseka.core.cucumber.hooks;

import dev.shendel.aseka.core.api.Cleanable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
public class CucumberListener {

    private static final ThreadLocal<String> PREV_FEATURE = new InheritableThreadLocal<>();

    private final List<Cleanable> cleanableBins;
    private final ApplicationEventPublisher eventPublisher;

    @Before(order = Integer.MIN_VALUE)
    public void publishFeatureStartEvent(Scenario scenario) {
        synchronized (PREV_FEATURE) {
            String currentFeature = scenario.getUri().toString();
            if (PREV_FEATURE.get() == null) {
                PREV_FEATURE.set(currentFeature);
                eventPublisher.publishEvent(new FeatureStartEvent(currentFeature));
            }
        }
    }

    @After(order = Integer.MIN_VALUE)
    public void publishFeatureEndEvent(Scenario scenario) {
        synchronized (PREV_FEATURE) {
            String currentFeature = scenario.getUri().toString();
            if (!currentFeature.equals(PREV_FEATURE.get())) {
                PREV_FEATURE.set(null);
                eventPublisher.publishEvent(new FeatureEndEvent(currentFeature));
            }
        }
    }

    @After("not @SaveContext")
    public void cleanContextAfterScenario() {
        log.info("Clearing context after scenario...");
        cleanableBins.forEach(Cleanable::clean);
    }

}
