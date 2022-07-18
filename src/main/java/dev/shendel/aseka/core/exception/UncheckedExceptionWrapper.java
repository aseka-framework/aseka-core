package dev.shendel.aseka.core.exception;

public class UncheckedExceptionWrapper extends Throwable {

    private UncheckedExceptionWrapper(Throwable cause) {
        super(cause);
    }

    public static UncheckedExceptionWrapper wrap(Throwable throwable) {
        return new UncheckedExceptionWrapper(throwable);
    }

    public Throwable unwrap() {
        return getCause();
    }

}
