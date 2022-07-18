package dev.shendel.aseka.core.matcher;

import dev.shendel.aseka.core.exception.AsekaException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum AsekaMatcher {
    ANY_STRING("anyString", false,true),
    ANY_NUMBER("anyNumber", false,false),
    ANY_BOOLEAN("anyBoolean", false,false),
    IGNORE("ignore", false,true),
    IGNORE_ELEMENT("ignoreElement", false,true),
    CONTAINS("contains", true,true),
    NOT_CONTAINS("!contains", true,true),
    NOT_BLANK_STRING("notBlank", false,true),
    IS_BLANK_STRING("isBlank", false,true),
    MATCHES_REGEX_STRING("matchesRegex", true,true),
    NOT_NULL("notNull", false,true),
    IS_NULL("isNull", false,true),
    EQUAL_TO("==", true,true),
    NOT_EQUAL_TO("!=", true,true),
    GREATER_THAN(">", true,false),
    GREATER_THAN_OR_EQUAL_TO(">=", true,false),
    LESS_THAN("<", true,false),
    LESS_THAN_OR_EQUAL_TO("<=", true,false);

    public static final String REGEX = "(contains|!contains|notBlank|isBlank|matchesRegex|notNull|isNull|==|!=|>|>=|<|<=)";

    private final String name;
    private final boolean needExpectedObject;
    private final boolean isStringMatcher;

    public static AsekaMatcher getBy(String name) {
        for (AsekaMatcher matcher : values()) {
            if (matcher.name.equalsIgnoreCase(name)) {
                return matcher;
            }
        }
        throw new AsekaException("Matcher '{}' is not supported. Supported matches: {}", name, getAvailableMatchers());
    }

    private static String getAvailableMatchers() {
        return Stream.of(values())
                .map(AsekaMatcher::getName)
                .collect(Collectors.joining(", "));
    }

}
