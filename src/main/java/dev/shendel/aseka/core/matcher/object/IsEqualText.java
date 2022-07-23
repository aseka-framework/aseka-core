package dev.shendel.aseka.core.matcher.object;

import dev.shendel.aseka.core.exception.AsekaAssertionError;
import dev.shendel.aseka.core.exception.AsekaException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

import static dev.shendel.aseka.core.matcher.object.IsEqualJson.DEFAULT_JSON_UNIT_CFG;

public class IsEqualText extends TypeSafeMatcher<String> {

    private final String expectedText;
    private boolean exactCompare;

    public static IsEqualText isEqualText(boolean exactCompare, String expectedText) {
        return new IsEqualText(exactCompare, expectedText);
    }

    public IsEqualText(boolean exactCompare, String expectedText) {
        this.exactCompare = exactCompare;
        this.expectedText = expectedText;
    }

    @Override
    protected boolean matchesSafely(String actualText) {
        if (exactCompare) {
            return actualText.equals(expectedText);
        } else {
            return actualText.contains(expectedText);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Text equals: \n " + expectedText);
    }

}
