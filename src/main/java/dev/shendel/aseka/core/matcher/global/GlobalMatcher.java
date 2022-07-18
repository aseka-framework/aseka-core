package dev.shendel.aseka.core.matcher.global;

public enum GlobalMatcher {
    JSON,
    XML;

    public static final String REGEX = "(JSON|XML)";

    public enum Config {
        DEFAULT_MATCHER,
        IGNORE_EXTRA_ELEMENTS
    }
}
