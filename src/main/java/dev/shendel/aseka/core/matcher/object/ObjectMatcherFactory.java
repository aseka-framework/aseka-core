package dev.shendel.aseka.core.matcher.object;

import dev.shendel.aseka.core.api.Cleanable;
import dev.shendel.aseka.core.exception.AsekaException;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObjectMatcherFactory implements Cleanable {
    //TODO add configurations from feature file and properties (ignore extra elements, default object matcher etc.)

    private static final ObjectMatcher DEFAULT_OBJECT_MATCHER = ObjectMatcher.JSON;

    private ObjectMatcher configuredObjectMatcher;

    public void setGlobalMatcher(ObjectMatcher objectMatcher) {
        configuredObjectMatcher = objectMatcher;
    }

    public Matcher<String> create(String expectedObject) {
        ObjectMatcher objectMatcher;

        if (configuredObjectMatcher != null) {
            objectMatcher = configuredObjectMatcher;
        } else if (isStartsLikeXml(expectedObject)) {
            objectMatcher = ObjectMatcher.XML;
        } else {
            objectMatcher = ObjectMatcher.JSON;
        }

        log.info("Creating object matcher by type: {}", objectMatcher);
        switch (objectMatcher) {
            case JSON:
                return IsEqualJson.isEqualJson(expectedObject);
            case XML:
                return IsEqualXml.isEqualXml(expectedObject);
            case CONTAINS_TEXT:
                return IsEqualText.isEqualText(false, expectedObject);
            case EXACT_TEXT:
                return IsEqualText.isEqualText(true, expectedObject);
            default:
                throw new AsekaException("Object matcher {} not supported", configuredObjectMatcher);
        }
    }

    private boolean isStartsLikeXml(String expectedObject) {
        return expectedObject.trim().startsWith("<");
    }

    @Override
    public void clean() {
        configuredObjectMatcher = DEFAULT_OBJECT_MATCHER;
    }

}
