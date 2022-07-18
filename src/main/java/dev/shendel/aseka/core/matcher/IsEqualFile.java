package dev.shendel.aseka.core.matcher;

import dev.shendel.aseka.core.exception.AsekaException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RequiredArgsConstructor
public class IsEqualFile extends TypeSafeMatcher<File> {

    private final File expectedFile;

    public static IsEqualFile isEqualFile(File expectedFilePath) {
        return new IsEqualFile(expectedFilePath);
    }

    @Override
    protected boolean matchesSafely(File actualFile) {
        boolean result;
        try (
                FileInputStream actual = new FileInputStream(actualFile);
                FileInputStream expected = new FileInputStream(expectedFile)
        ) {
            result = IOUtils.contentEquals(actual, expected);
        } catch (IOException e) {
            throw new AsekaException("Can't compare files", e);
        }
        return result;
    }

    @Override
    public void describeTo(Description description) {
    }
}
