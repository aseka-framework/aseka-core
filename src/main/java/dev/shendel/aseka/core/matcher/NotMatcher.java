package dev.shendel.aseka.core.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class NotMatcher<T> extends TypeSafeMatcher<T> {

    private final Matcher<T> matcher;

    public NotMatcher(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return !matcher.matches(item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("not ").appendDescriptionOf(matcher);
    }

    public static <T> Matcher<T> not(Matcher<T> matcher) {
        return new NotMatcher<>(matcher);
    }
}
