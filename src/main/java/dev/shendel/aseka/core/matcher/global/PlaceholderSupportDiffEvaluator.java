package dev.shendel.aseka.core.matcher.global;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xmlunit.diff.ComparisonResult.EQUAL;

//TODO REWRITE
public class PlaceholderSupportDiffEvaluator implements DifferenceEvaluator {
    private static final String ANY_NUMBER_PLACEHOLDER = "#{json-unit.any-number}";
    private static final String ANY_BOOLEAN_PLACEHOLDER = "#{json-unit.any-boolean}";
    private static final String ANY_STRING_PLACEHOLDER = "#{json-unit.any-string}";
    private static final String IGNORE_PLACEHOLDER = "#{json-unit.ignore}";
    private static final String REGEX_PLACEHOLDER = "#{json-unit.regex}";
    private static final String CUSTOM_MATCHER_PLACEHOLDER = "#{json-unit.matches:";
    private static final Pattern CUSTOM_MATCHER_PLACEHOLDER_PATTERN = Pattern.compile(
            "#\\{json-unit.matches:(.+)\\}(.*)");
    //FIXME temporary(HA!) reuse json-unit cfg for matchers retrieving
    private final Configuration configuration;

    public PlaceholderSupportDiffEvaluator(Configuration jsonUnitCfg) {
        this.configuration = jsonUnitCfg;
    }

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == EQUAL) {
            return outcome;
        }
        final Node expected = comparison.getControlDetails().getTarget();
        final Node actual = comparison.getTestDetails().getTarget();
        if (expected instanceof Text && actual instanceof Text || expected instanceof Attr && actual instanceof Attr) {
            if (check(expected.getTextContent(), actual.getTextContent())) {
                return EQUAL;
            }
        }
        return outcome;
    }

    private boolean check(String expected, String actual) {
        if (expected.equals(ANY_NUMBER_PLACEHOLDER)) {
            return NumberUtils.isParsable(actual);
        }
        if (expected.equals(ANY_BOOLEAN_PLACEHOLDER)) {
            return "true".equalsIgnoreCase(actual) || "false".equals(actual);
        }
        if (expected.equals(ANY_STRING_PLACEHOLDER)) {
            return actual != null;
        }
        //FIXME почему нужен слэш?
        if (expected.startsWith(REGEX_PLACEHOLDER)) {
            return actual.matches(regexPattern(expected));
        }
        if (expected.startsWith(CUSTOM_MATCHER_PLACEHOLDER)) {
            return compareWithCustomMatcher(expected, actual);
        }
        return IGNORE_PLACEHOLDER.equals(expected);
    }

    private String regexPattern(String val) {
        return val.substring(REGEX_PLACEHOLDER.length());
    }

    private boolean compareWithCustomMatcher(String expected, String actual) {
        Matcher patternMatcher = CUSTOM_MATCHER_PLACEHOLDER_PATTERN.matcher(expected);
        if (patternMatcher.matches()) {
            String matcherName = patternMatcher.group(1);
            org.hamcrest.Matcher<?> matcher = configuration.getMatcher(matcherName);
            if (matcher != null) {
                if (matcher instanceof ParametrizedMatcher) {
                    ((ParametrizedMatcher) matcher).setParameter(patternMatcher.group(2));
                }
                return matcher.matches(actual);
            }
            throw new MatcherNotFound("Matcher \"" + matcherName + "\" not found.");
        }
        return false;
    }

    private static class MatcherNotFound extends RuntimeException {
        MatcherNotFound(String s) {
            super(s);
        }
    }

}
