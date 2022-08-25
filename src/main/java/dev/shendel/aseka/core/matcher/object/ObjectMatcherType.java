package dev.shendel.aseka.core.matcher.object;

public enum ObjectMatcherType {
    JSON,
    XML,
    CONTAINS_TEXT,
    EXACT_TEXT;

    public static final String REGEX = "(JSON|XML|CONTAINS_TEXT|EXACT_TEXT)";

//    public enum Config {
//        DEFAULT_MATCHER,
//        IGNORE_EXTRA_ELEMENTS
//    }
}
