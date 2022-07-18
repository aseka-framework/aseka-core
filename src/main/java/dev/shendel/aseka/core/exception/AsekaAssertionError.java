package dev.shendel.aseka.core.exception;

import dev.shendel.aseka.core.util.StringUtil;

public class AsekaAssertionError extends AssertionError {

    public AsekaAssertionError(String message, Object... args) {
        super(StringUtil.format(message, args));
    }

    public AsekaAssertionError(String message, Throwable cause, Object... args) {
        super(StringUtil.format(message, args), cause);
    }

}
