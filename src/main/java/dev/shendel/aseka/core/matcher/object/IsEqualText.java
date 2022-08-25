package dev.shendel.aseka.core.matcher.object;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class IsEqualText extends TypeSafeMatcher<String> {

    private final String expectedText;
    private final boolean exactCompare;

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
