package dev.shendel.aseka.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String STRING_REGEX = "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"";
    public static final String STRING_REGEX_2 = "'([^'\\\\]*(\\\\.[^'\\\\]*)*)'";
    public static final String STRING_ARRAY_REGEX = "\\[(\"([^\"]*)\"(, |))+\\]"; // Строка не может содержать ковычки
    public static final String INT_REGEX = "(\\d+)";
    public static final String INT_ARRAY_REGEX = "\\[((\\d+)(, |))+\\]";
    public static final String FLOAT_REGEX = "(\\d+\\.\\d+)";
    public static final String FLOAT_ARRAY_REGEX = "\\[((\\d+\\.\\d+)(, |))+\\]";
    public static final String HTTP_METHOD_REGEX = "(GET|PUT|POST|DELETE|HEAD|TRACE|OPTIONS|PATCH)";
    public static final String NOT_USED = "notUsed";

    public static final String DEFAULT_GLUE = "dev.shendel";
    public static final String DEFAULT_TAGS = "not (@Skip or @Demo)";
    public static final String DEFAULT_FEATURES = "classpath:features";
    public static final String ALLURE_PLUGIN = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm";

}
