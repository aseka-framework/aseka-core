package dev.shendel.aseka.core.cucumber.hooks;

import org.springframework.context.ApplicationEvent;

public class FeatureStartEvent extends ApplicationEvent {

    public FeatureStartEvent(String featureUri) {
        super(featureUri);
    }

}
