package dev.shendel.aseka.core.matcher.global;

import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.matcher.AsekaMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GlobalMatcherHelper {

    public static final String DELIMITER = "::";

    //TODO add xml support
    public static Function<String, String> getFunction() {
        return (String parameter) -> {
            String[] split = parameter.split(DELIMITER);
            String matcherName = split[0];
            AsekaMatcher asekaMatcher = AsekaMatcher.getBy(matcherName);

            switch (asekaMatcher) {
                case ANY_STRING:
                    return "#{json-unit.any-string}";
                case ANY_NUMBER:
                    return "#{json-unit.any-number}";
                case ANY_BOOLEAN:
                    return "#{json-unit.any-boolean}";
                case IGNORE:
                    return "#{json-unit.ignore}";
                case IGNORE_ELEMENT:
                    return "#{json-unit.ignore-element}";
                case CONTAINS:
                case NOT_CONTAINS: {
                    String stingParam = split[1];
                    //TODO add readable error for out of bound
                    return buildCustomMatcherPlaceholder(asekaMatcher.getName(), stingParam);
                }
                case MATCHES_REGEX_STRING: {
                    String regex = split[1];
                    return "#{json-unit.regex}" + regex;
                }
                case NOT_BLANK_STRING:
                case IS_BLANK_STRING:
                    return buildCustomMatcherPlaceholder(asekaMatcher.getName(), null);
            }

            throw new AsekaException("matcher '{}' is not supported", matcherName);
        };
    }

    @NotNull
    private static String buildCustomMatcherPlaceholder(String jsonUnitMatcherName, String parameter) {
        return "#{json-unit.matches:" + jsonUnitMatcherName + "}" + parameter;
    }

}
