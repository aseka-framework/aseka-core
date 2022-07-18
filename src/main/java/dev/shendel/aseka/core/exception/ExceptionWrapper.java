package dev.shendel.aseka.core.exception;

public class ExceptionWrapper extends Exception {

    private ExceptionWrapper(Throwable cause) {
        super(cause);
    }

    public static ExceptionWrapper wrap(Throwable throwable) {
        return new ExceptionWrapper(throwable);
    }

    public Throwable unwrap() {
        return getCause();
    }

    public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

}
