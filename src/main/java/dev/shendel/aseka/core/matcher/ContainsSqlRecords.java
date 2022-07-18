package dev.shendel.aseka.core.matcher;

import dev.shendel.aseka.core.cucumber.parser.MatcherFactory;
import dev.shendel.aseka.core.cucumber.parser.Type;
import dev.shendel.aseka.core.exception.AsekaException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static dev.shendel.aseka.core.matcher.AsekaMatcher.EQUAL_TO;
import static dev.shendel.aseka.core.util.Asserts.assertThat;
import static dev.shendel.aseka.core.util.Validator.checkThat;
import static java.util.stream.Collectors.toList;


public class ContainsSqlRecords extends TypeSafeMatcher<List<Map<String, Object>>> {

    public static final String MATCHER_EXPRESSION = "matcher:";
    private final List<Map<String, String>> expectedResults;

    private ContainsSqlRecords(List<Map<String, String>> expectedResults) {
        this.expectedResults = convertKeysToUpperCase(expectedResults);
    }

    public static ContainsSqlRecords containsSqlRecords(List<Map<String, String>> expectedRecords) {
        return new ContainsSqlRecords(expectedRecords);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("SQL contains: \n " + expectedResults + " \n");
    }

    @Override
    //TODO если запись на проверку была одно - выводить, в каком поле ошибка...
    protected boolean matchesSafely(List<Map<String, Object>> records) {
        List<Map<String, Object>> actualRecords = convertKeysToUpperCase(records);
        expectedResults.stream()
                .map(Map::entrySet)
                .forEach(
                        expectedRecord -> {
                            boolean recordFound = actualRecords.stream().anyMatch(
                                    actualRecord -> expectedRecord.stream().allMatch(containColumn(actualRecord))
                            );
                            assertThat(
                                    recordFound,
                                    "Record '{}' not found. \nActual records: {}",
                                    expectedRecord,
                                    actualRecords
                            );
                        }
                );

        return true;
    }

    private static Predicate<Map.Entry<String, String>> containColumn(Map<String, Object> actualRecord) {
        return expectedColumn -> {
            String columnName = expectedColumn.getKey();
            if (!actualRecord.containsKey(columnName)) {
                return false;
            }

            String expectedExpression = expectedColumn.getValue();
            Object expectedValue = null;
            Object actualValue = actualRecord.get(columnName);

            if (hasMatcher(expectedExpression)) {
                String matcherName = getMatcherName(expectedExpression);
                AsekaMatcher asekaMatcher = AsekaMatcher.getBy(matcherName);
                if (asekaMatcher.isNeedExpectedObject()) {
                    String stringExpectedValue = getStringExpectedValue(expectedExpression);
                    expectedValue = parseObjectFromString(stringExpectedValue, actualValue);
                }
                return MatcherFactory.create(asekaMatcher, expectedValue).matches(actualValue);
            } else {
                Matcher<?> matcher = MatcherFactory.create(EQUAL_TO, expectedExpression);
                return matcher.matches(actualValue != null ? actualValue.toString() : null);
            }
        };
    }

    //TODO move to Type class
    private static Object parseObjectFromString(String stringValue, Object objectWithCorrectType) {
        if (objectWithCorrectType instanceof String) {
            return Type.parseObject(Type.STRING, stringValue);
        } else if (objectWithCorrectType instanceof BigDecimal) {
            return Type.parseObject(Type.BIG_DECIMAL, stringValue);
        } else if (objectWithCorrectType instanceof Timestamp) {
            return Type.parseObject(Type.TIMESTAMP, stringValue);
        } else if (objectWithCorrectType instanceof Boolean) {
            return Type.parseObject(Type.BOOLEAN, stringValue);
        } else if (objectWithCorrectType instanceof Integer) {
            return Type.parseObject(Type.INT, stringValue);
        } else if (objectWithCorrectType instanceof Float) {
            return Type.parseObject(Type.FLOAT, stringValue);
        } else if (objectWithCorrectType instanceof Double) {
            return Type.parseObject(Type.DOUBLE, stringValue);
        } else {
            throw new AsekaException(
                    "Object with type '{}' is not supported",
                    objectWithCorrectType.getClass().getSimpleName()
            );
        }
    }

    private static boolean hasMatcher(String expectedExpression) {
        return expectedExpression.startsWith(MATCHER_EXPRESSION);
    }

    private static String getMatcherName(String expression) {
        String[] strings = expression.split("::");
        checkThat(strings.length < 3, "Wrong matcher format");
        return strings[0].substring(MATCHER_EXPRESSION.length());
    }

    private static String getStringExpectedValue(String expression) {
        String[] strings = expression.split("::");
        checkThat(strings.length > 1, "Wrong matcher format");
        return strings[1];
    }

    public static <V> List<Map<String, V>> convertKeysToUpperCase(List<Map<String, V>> resultMapList) {
        return resultMapList.stream()
                .map(mapRecord -> mapRecord.entrySet()
                        .stream()
                        .collect(
                                HashMap<String, V>::new,
                                (m, v) -> m.put(v.getKey().toUpperCase(), v.getValue()),
                                HashMap::putAll
                        ))
                .collect(toList());
    }

}
