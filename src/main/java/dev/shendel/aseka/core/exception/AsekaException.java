package dev.shendel.aseka.core.exception;

import dev.shendel.aseka.core.util.StringUtil;

public class AsekaException extends RuntimeException {

    public AsekaException(String message, Object... args) {
        super(StringUtil.format(message, args));
    }

    public AsekaException(String message, Throwable cause, Object... args) {
        super(StringUtil.format(message, args), cause);
    }

}
