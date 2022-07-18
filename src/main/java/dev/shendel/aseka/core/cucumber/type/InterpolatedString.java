package dev.shendel.aseka.core.cucumber.type;


public class InterpolatedString {

    private final String value;

    private InterpolatedString(String value) {
        this.value = value;
    }

    public static InterpolatedString of(String value) {
        return new InterpolatedString(value);
    }

    public String get() {
        return value;
    }

}
