package dev.shendel.aseka.core.cucumber.type;

import dev.shendel.aseka.core.Constants;
import dev.shendel.aseka.core.cucumber.parser.MatcherFactory;
import dev.shendel.aseka.core.cucumber.parser.Type;
import dev.shendel.aseka.core.extension.db.SqlScriptType;
import dev.shendel.aseka.core.matcher.AsekaMatcher;
import dev.shendel.aseka.core.matcher.object.ObjectMatcher;
import dev.shendel.aseka.core.service.StringInterpolator;
import dev.shendel.aseka.core.util.StringUtil;
import io.cucumber.java.DataTableType;
import io.cucumber.java.DocStringType;
import io.cucumber.java.ParameterType;
import io.qameta.allure.Allure;
import io.restassured.http.Header;
import io.restassured.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;

import java.util.List;

import static dev.shendel.aseka.core.util.Validator.checkThat;

@Slf4j
@RequiredArgsConstructor
public class CucumberTypesDefinition {

    private final StringInterpolator stringInterpolator;

    @ParameterType(name = "interpolated_string", value = Constants.STRING_REGEX_2)
    public String interpolated_string(String value) {
        return stringInterpolator.interpolate(StringUtil.trimQuotes(value))
                .replaceAll("\\\\\"", "\"")
                .replaceAll("\\\\'", "'");
    }

    @ParameterType(name = "file_path", value = Constants.STRING_REGEX_2)
    public String file_path(String value) {
        return stringInterpolator.interpolate(StringUtil.trimQuotes(value))
                .replaceAll("\\\\\"", "\"")
                .replaceAll("\\\\'", "'");
    }

    @ParameterType(name = "http_method", value = Constants.HTTP_METHOD_REGEX)
    public Method http_method(String value) {
        return Method.valueOf(value);
    }

    @ParameterType(name = "sql_script_type", value = SqlScriptType.REGEX)
    public SqlScriptType sql_script_type(String value) {
        return SqlScriptType.valueOf(value);
    }

    @ParameterType(name = "matcher", value = AsekaMatcher.REGEX)
    public AsekaMatcher matcher(String matcherSymbol) {
        return AsekaMatcher.getBy(matcherSymbol);
    }

    @ParameterType(name = "global_matcher", value = ObjectMatcher.REGEX)
    public ObjectMatcher global_matcher(String globalMatcherName) {
        return ObjectMatcher.valueOf(globalMatcherName);
    }

    @DataTableType
    public Header parseHeader(List<String> header) {
        String name = stringInterpolator.interpolate(header.get(0));
        String value = stringInterpolator.interpolate(header.get(1));

        return new Header(name, value);
    }

    @DocStringType
    public InterpolatedString interpolatedString(String input) {
        String value = stringInterpolator.interpolate(input);
        Allure.addAttachment("content", value);
        return InterpolatedString.of(value);
    }

    @DataTableType
    public InterpolatedString singleTableRow(List<String> columns) {
        checkThat(columns.size() == 1, "Table must have only 1 column");

        String value = stringInterpolator.interpolate(columns.get(0));

        return InterpolatedString.of(value);
    }

    @DataTableType
    public Pair pairTableRow(List<String> columns) {
        checkThat(columns.size() == 2, "Table must have only 2 column");

        String first = stringInterpolator.interpolate(columns.get(0));
        String second = stringInterpolator.interpolate(columns.get(1));

        return Pair.of(first, second);
    }

    @DataTableType
    public Triple tripleTableRow(List<String> columns) {
        checkThat(columns.size() == 3, "Table must have only 3 column");

        String first = stringInterpolator.interpolate(columns.get(0));
        String second = stringInterpolator.interpolate(columns.get(1));
        String third = stringInterpolator.interpolate(columns.get(2));

        return Triple.of(first, second, third);
    }

    @DataTableType
    public HttpBodyValidator bodyValidatorRow(List<String> columns) {
        checkThat(columns.size() == 3, "Table must have only 3 column");
        String selector = stringInterpolator.interpolate(columns.get(0));
        String matcherName = stringInterpolator.interpolate(columns.get(1));
        String operand = stringInterpolator.interpolate(columns.get(2));

        Type objectType = Type.getByOperand(operand);
        Object expectedObject = objectType.parseObject(StringUtil.trimQuotes(operand));
        AsekaMatcher asekaMatcher = AsekaMatcher.getBy(matcherName);

        Matcher<?> matcher = MatcherFactory.create(asekaMatcher, expectedObject);
        return new HttpBodyValidator(selector, matcher);
    }

}
