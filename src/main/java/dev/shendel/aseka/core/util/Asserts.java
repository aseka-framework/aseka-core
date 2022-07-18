package dev.shendel.aseka.core.util;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;

@UtilityClass
public class Asserts {

    public static void assertThat(boolean assertion, String message, Object... args) {
        if (!assertion) {
            throwAssert(message, args);
        }
    }

    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        org.hamcrest.MatcherAssert.assertThat("", actual, matcher);
    }

    public static <T> void assertThat(T actual, Matcher<? super T> matcher, String message, Object... args) {
        if (!matcher.matches(actual)) {
            throwAssert(message, args);
        }
    }

    public static void throwAssert(String message, Object[] args) {
        throw new AssertionError(StringUtil.format(message, args));
    }

}
