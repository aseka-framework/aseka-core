package dev.shendel.aseka.core.matcher.object;

import dev.shendel.aseka.core.exception.AsekaAssertionError;
import dev.shendel.aseka.core.exception.AsekaException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.NodeMatcher;

import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

import static dev.shendel.aseka.core.matcher.object.IsEqualJson.DEFAULT_JSON_UNIT_CFG;
import static org.xmlunit.diff.ElementSelectors.byName;
import static org.xmlunit.diff.ElementSelectors.byNameAndText;

public class IsEqualXml extends TypeSafeMatcher<String> {

    private final NodeMatcher DEFAULT_NODE_MATCHER = new DefaultNodeMatcher(byNameAndText, byName);
    private final String expectedXml;
    private String differenceDescription;

    public IsEqualXml(String expectedXml) {
        validateXml(expectedXml);

        this.expectedXml = expectedXml;
    }

    private void validateXml(String expectedXml) {
        try {
            SAXParserFactory.newInstance()
                    .newSAXParser()
                    .getXMLReader()
                    .parse(new InputSource(new StringReader(expectedXml)));
        } catch (Exception exception) {
            throw new AsekaException("Can't parse expected xml.", exception);
        }
    }

    public static IsEqualXml isEqualXml(String expectedXml) {
        return new IsEqualXml(expectedXml);
    }

    @Override
    protected boolean matchesSafely(String actualXmlString) {
        try {
            Diff diff = DiffBuilder.compare(expectedXml)
                    .withNodeMatcher(DEFAULT_NODE_MATCHER)
                    .withTest(actualXmlString)
                    .ignoreComments()
                    .ignoreWhitespace()
                    .normalizeWhitespace()
                    .checkForSimilar()
                    .withDifferenceEvaluator(
                            DifferenceEvaluators.chain(
                                    DifferenceEvaluators.Default,
                                    new PlaceholderSupportDiffEvaluator(DEFAULT_JSON_UNIT_CFG)
                            ))
                    .build();

            if (!diff.hasDifferences()) {
                return true;
            } else {
                differenceDescription = diff.fullDescription();
                return false;
            }
        } catch (Exception e) {
            throw new AsekaAssertionError("Can't compare XML", e);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("XML equals: \n " + expectedXml + "\n" + differenceDescription);
    }

}
