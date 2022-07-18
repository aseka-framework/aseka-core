package dev.shendel.aseka.core.matcher.global;

import dev.shendel.aseka.core.matcher.AsekaMatcher;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.internal.Diff;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.internal.Diff.create;

@RequiredArgsConstructor
//TODO create from MatcherFactory
public class IsEqualJson extends TypeSafeMatcher<String> {

    private static final String FULL_JSON = "fullJson";
    private static final String ROOT = "";
    //TODO move to factory
    public static final Configuration DEFAULT_JSON_UNIT_CFG = Configuration.empty()
            .withOptions(IGNORING_ARRAY_ORDER)
            .withOptions(IGNORING_EXTRA_FIELDS)
            .withMatcher(AsekaMatcher.CONTAINS.getName(), JsonUnitMatchers.containsString())
            .withMatcher(AsekaMatcher.NOT_CONTAINS.getName(), JsonUnitMatchers.notContainsString())
            .withMatcher(AsekaMatcher.NOT_BLANK_STRING.getName(), JsonUnitMatchers.notBlank())
            .withMatcher(AsekaMatcher.IS_BLANK_STRING.getName(), JsonUnitMatchers.isBlank());
    //            .withMatcher("formattedAs", new DateFormatMatcher())
    //            .withMatcher("formattedAndWithin", DateWithin.Companion.param())
    //            .withMatcher("formattedAndWithinNow", DateWithin.Companion.now())
    //            .withMatcher("xmlDateWithinNow", new XMLDateWithin());

    private final String expectedJson;
    private String difference;

    public static IsEqualJson isEqualJson(String expectedJson) {
        return new IsEqualJson(expectedJson);
    }

    @Override
    protected boolean matchesSafely(String actualJson) {
        Diff diff = create(expectedJson, actualJson, FULL_JSON, ROOT, DEFAULT_JSON_UNIT_CFG);
        if (diff.similar()) {
            return true;
        } else {
            difference = diff.differences();
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expectedJson + "\n " + difference);
    }

}
