package dev.shendel.aseka.core.cucumber.hooks;

import org.springframework.context.ApplicationEvent;

public class FeatureEndEvent extends ApplicationEvent {

    public FeatureEndEvent(String featureUri) {
        super(featureUri);
    }

}
