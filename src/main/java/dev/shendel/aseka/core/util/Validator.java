package dev.shendel.aseka.core.util;

import lombok.experimental.UtilityClass;
import dev.shendel.aseka.core.exception.AsekaException;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

@UtilityClass
public class Validator {

    public static void checkThat(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new AsekaException(message, args);
        }
    }

    public static void checkThatNotNull(Object object, String message) {
        if (object == null) {
            throw new AsekaException(message);
        }
    }

    public static void checkThatNotBlank(String string, String message) {
        if (isBlank(string)) {
            throw new AsekaException(message);
        }
    }

    public static void checkThatAllNotBlank(String message, String... strings) {
        if (isAnyBlank(strings)) {
            throw new AsekaException(message);
        }
    }

    public static void checkDownloadFolder(String destination, String expectedFolder) {
        checkThat(destination.startsWith(expectedFolder), "File can downloaded only into:'{}'", expectedFolder);
    }

}
