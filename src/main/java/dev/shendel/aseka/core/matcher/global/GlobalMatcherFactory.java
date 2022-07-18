package dev.shendel.aseka.core.matcher.global;

import dev.shendel.aseka.core.api.Cleanable;
import dev.shendel.aseka.core.exception.AsekaException;
import org.hamcrest.Matcher;
import org.springframework.stereotype.Service;

@Service
public class GlobalMatcherFactory implements Cleanable {
    //TODO add configurations from feature file and properties (ignore extra elements, default global matcher etc.)
    //MAKE fail on extra elements default behavior

    private static final GlobalMatcher DEFAULT_GLOBAL_MATCHER = GlobalMatcher.JSON;

    private GlobalMatcher currentGlobalMatcher = DEFAULT_GLOBAL_MATCHER;

    public void setGlobalMatcher(GlobalMatcher globalMatcher) {
        currentGlobalMatcher = globalMatcher;
    }

    public Matcher<String> create(String expected) {
        //TODO можно сделать автоопределение
        switch (currentGlobalMatcher) {
            case JSON:
                return IsEqualJson.isEqualJson(expected);
            case XML:
                return IsEqualXml.isEqualXml(expected);
            default:
                throw new AsekaException("Global matcher {} not supported", currentGlobalMatcher);
        }
    }

    @Override
    public void clean() {
        currentGlobalMatcher = DEFAULT_GLOBAL_MATCHER;
    }

}
