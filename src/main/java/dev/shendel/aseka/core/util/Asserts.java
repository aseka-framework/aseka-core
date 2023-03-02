package dev.shendel.aseka.core.util;

import lombok.experimental.UtilityClass;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

@UtilityClass
public class Asserts {

    public static void assertThat(boolean assertion, String message, Object... args) {
        if (!assertion) {
            throwAssert(message, args);
        }
    }

    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        if (!matcher.matches(actual)) {
            Description description = new StringDescription();
            description.appendText("")
                    .appendText(System.lineSeparator())
                    .appendText("Expected: ")
                    .appendDescriptionOf(matcher)
                    .appendText(System.lineSeparator())
                    .appendText("     but: ");
            matcher.describeMismatch(actual, description);

            throw new AssertionError(description.toString());
        }
    }

    public static <T> void assertThat(T actual, Matcher<? super T> matcher, String message, Object... args) {
        if (!matcher.matches(actual)) {
            throwAssert(message, args);
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if ( !condition ) {
            throw new AssertionError(StringUtil.format(message));
        }
    }

    private static void throwAssert(String message, Object[] args) {
        throw new AssertionError(StringUtil.format(message, args));
    }

}
