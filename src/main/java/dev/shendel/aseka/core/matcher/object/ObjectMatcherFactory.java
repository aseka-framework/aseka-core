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
    private ObjectMatcherType currentObjectMatcherType;

    public void setObjectMatcherType(ObjectMatcherType objectMatcherType) {
        currentObjectMatcherType = objectMatcherType;
    }

    public Matcher<String> isEqualObject(String expectedObject) {
        ObjectMatcherType type = getObjectMatcherType(expectedObject);
        switch (type) {
            case JSON:
                return IsEqualJson.isEqualJson(expectedObject);
            case XML:
                return IsEqualXml.isEqualXml(expectedObject);
            case CONTAINS_TEXT:
                return IsEqualText.isEqualText(false, expectedObject);
            case EXACT_TEXT:
                return IsEqualText.isEqualText(true, expectedObject);
            default:
                throw new AsekaException("Object matcher {} not supported", currentObjectMatcherType);
        }
    }

    private ObjectMatcherType getObjectMatcherType(String expectedObject) {
        if (currentObjectMatcherType != null) {
            log.info("Object matcher defined manually: {}", currentObjectMatcherType);
            return currentObjectMatcherType;
        } else if (isXmlObject(expectedObject)) {
            return ObjectMatcherType.XML;
        } else {
            return ObjectMatcherType.JSON;
        }
    }

    private boolean isXmlObject(String expectedObject) {
        return expectedObject.trim().startsWith("<");
    }

    @Override
    public void clean() {
        currentObjectMatcherType = null;
    }
}
