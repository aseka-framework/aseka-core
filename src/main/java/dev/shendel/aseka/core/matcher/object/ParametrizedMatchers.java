package dev.shendel.aseka.core.matcher.object;

import lombok.experimental.UtilityClass;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.Function;

@UtilityClass
public class ParametrizedMatchers {

    public static Matcher<?> containsString() {
        return ParametrizedMatcher.createWithDelegate(Matchers::containsString);
    }

    public static Matcher<?> notContainsString() {
        return ParametrizedMatcher.createWithDelegate((substring) -> Matchers.not(Matchers.containsString(substring)));
    }

    public static Matcher<?> notBlank() {
        return ParametrizedMatcher.createWithDelegate((ignore) -> Matchers.not(Matchers.blankString()));
    }

    public static Matcher<?> isBlank() {
        return ParametrizedMatcher.createWithDelegate((ignore) -> Matchers.blankString());
    }

    private static class ParametrizedMatcher extends TypeSafeMatcher<String> implements net.javacrumbs.jsonunit.core.ParametrizedMatcher {

        private final Function<String, Matcher<?>> delegateSupplier;
        private Matcher<?> delegate;

        public static ParametrizedMatcher createWithDelegate(Function<String, Matcher<?>> delegateSupplier) {
            return new ParametrizedMatcher(delegateSupplier);
        }

        private ParametrizedMatcher(Function<String, Matcher<?>> delegateSupplier) {
            this.delegateSupplier = delegateSupplier;
        }

        @Override
        public void setParameter(String substring) {
            delegate = delegateSupplier.apply(substring);
        }

        @Override
        protected boolean matchesSafely(String item) {
            return delegate.matches(item);
        }

        @Override
        public void describeTo(Description description) {
            delegate.describeTo(description);
        }
    }

}
