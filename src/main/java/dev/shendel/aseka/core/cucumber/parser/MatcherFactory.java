package dev.shendel.aseka.core.cucumber.parser;

import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.matcher.AsekaMatcher;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.blankString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@UtilityClass
public class MatcherFactory {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Matcher create(AsekaMatcher asekaMatcher, Object expectedObject) {
        switch (asekaMatcher) {
            case ANY_STRING:
                return Matchers.allOf(
                        Matchers.instanceOf(String.class),
                        not(Matchers.blankString())
                );
            case ANY_NUMBER:
                return Matchers.anyOf(
                        Matchers.instanceOf(BigDecimal.class),
                        Matchers.instanceOf(Integer.class),
                        Matchers.instanceOf(Long.class),
                        Matchers.instanceOf(Float.class),
                        Matchers.instanceOf(Double.class)
                );
            case ANY_BOOLEAN:
                return Matchers.instanceOf(Boolean.class);
            case CONTAINS:
                return containsString((String) expectedObject);
            case NOT_CONTAINS:
                return not(containsString((String) expectedObject));
            case IS_BLANK_STRING:
                return is(blankString());
            case NOT_BLANK_STRING:
                return not(blankString());
            case MATCHES_REGEX_STRING:
                return matchesPattern((String) expectedObject);
            case IS_NULL:
                return is(nullValue());
            case NOT_NULL:
                return is(notNullValue());
            case EQUAL_TO:
                return equalTo(expectedObject);
            case NOT_EQUAL_TO:
                return not(equalTo(expectedObject));
            case GREATER_THAN:
                return greaterThan((Comparable) expectedObject);
            case GREATER_THAN_OR_EQUAL_TO:
                return greaterThanOrEqualTo((Comparable) expectedObject);
            case LESS_THAN:
                return lessThan((Comparable) expectedObject);
            case LESS_THAN_OR_EQUAL_TO:
                return lessThanOrEqualTo((Comparable) expectedObject);
            default:
                throw new AsekaException("matcher '{}' isn't supported. Supported matchers: {}", asekaMatcher);
        }
    }

}
