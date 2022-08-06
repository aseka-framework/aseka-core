package dev.shendel.aseka.core.matcher;

import com.google.common.collect.Lists;
import dev.shendel.aseka.core.cucumber.parser.MatcherFactory;
import dev.shendel.aseka.core.cucumber.parser.Type;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static dev.shendel.aseka.core.matcher.AsekaMatcher.EQUAL_TO;
import static dev.shendel.aseka.core.util.Asserts.assertThat;
import static dev.shendel.aseka.core.util.Validator.checkThat;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public class ContainsSqlRecords extends TypeSafeMatcher<List<Map<String, Object>>> {

    public static final String MATCHER_EXPRESSION = "matcher:";
    private final List<Map<String, String>> expectedRecords;

    private ContainsSqlRecords(List<Map<String, String>> expectedRecords) {
        if (expectedRecords == null) {
            throw new AsekaException("expectedRecords can't be null");
        }
        this.expectedRecords = convertKeysToUpperCase(expectedRecords);
    }

    public static ContainsSqlRecords containsSqlRecords(List<Map<String, String>> expectedRecords) {
        return new ContainsSqlRecords(expectedRecords);
    }

    @Override
    public void describeTo(Description description) {
    }

    @Override
    protected boolean matchesSafely(List<Map<String, Object>> records) {
        if (records == null) {
            records = new ArrayList<>();
        }
        List<Map<String, Object>> actualRecords = convertKeysToUpperCase(records);
        assertThat(
                records.size() == expectedRecords.size(),
                "Expected {} but was {} records.\n" +
                        "Expected:\n{}\n" +
                        "Actual:\n{}",
                expectedRecords.size(),
                actualRecords.size(),
                formatLikeATable(expectedRecords),
                formatLikeATable(actualRecords)
        );

        expectedRecords.stream()
                .map(Map::entrySet)
                .forEach(
                        expectedRecord -> {
                            boolean recordFound = isRecordFound(expectedRecord, actualRecords);
                            if (expectedRecords.size() == 1) {
                                String mismatchedColumns = describeColumnMismatchesForOneRow(
                                        expectedRecord,
                                        actualRecords
                                );
                                assertThat(
                                        recordFound,
                                        "Expected record not matched actual.\n" +
                                                "Expected:\n{}\n" +
                                                "Actual:\n{}\n" +
                                                "Mismatched columns:\n{}\n",
                                        formatLikeATable(expectedRecords),
                                        formatLikeATable(actualRecords),
                                        mismatchedColumns
                                );
                            } else {
                                assertThat(
                                        recordFound,
                                        "Expected record not found\n" +
                                                "Expected record:\n{}\n" +
                                                "Actual records:\n{}\n",
                                        formatLikeATable(expectedRecord),
                                        formatLikeATable(actualRecords)
                                );
                            }
                        }
                );

        return true;
    }


    private String describeColumnMismatchesForOneRow(Set<Map.Entry<String, String>> expectedRecord,
                                                     List<Map<String, Object>> actualRecords) {
        StringJoiner description = new StringJoiner("\n");
        Map<String, Object> actualRecord = actualRecords.get(0);
        for (Map.Entry<String, String> expectedColumn : expectedRecord) {
            if (!containColumn(actualRecord).test(expectedColumn)) {
                final String columnName = expectedColumn.getKey();
                String columnMismatches = StringUtil.format(
                        "{} must be <{}> but was <{}>",
                        columnName,
                        expectedColumn.getValue(),
                        actualRecord.get(expectedColumn.getKey())
                );
                description.add(columnMismatches);
            }
        }

        return description.toString();
    }

    private boolean isRecordFound(Set<Map.Entry<String, String>> expectedRecord,
                                  List<Map<String, Object>> actualRecords) {
        return actualRecords.stream()
                .anyMatch(actualRecord -> expectedRecord.stream().allMatch(containColumn(actualRecord)));
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
                                LinkedHashMap<String, V>::new,
                                (m, v) -> m.put(v.getKey().toUpperCase(), v.getValue()),
                                LinkedHashMap::putAll
                        ))
                .collect(toList());
    }

    private String formatLikeATable(Set<Map.Entry<String, String>> expectedRecord) {
        Map<String, String> record = expectedRecord.stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return formatLikeATable(Lists.newArrayList(record));
    }

    public <T> String formatLikeATable(List<Map<String, T>> records) {
        if (records.isEmpty()) {
            return "empty table";
        }
        Set<String> columnNames = records.get(0).keySet();
        boolean allHaveSameColumns = records.stream()
                .allMatch(row -> row.keySet().containsAll(columnNames) && row.keySet().size() == columnNames.size());

        if (!allHaveSameColumns) {
            return records.toString();
        }

        Map<String, Integer> columnMaxSizes = new HashMap<>();

        for (Map<String, T> record : records) {
            for (String columnName : columnNames) {
                columnMaxSizes.putIfAbsent(columnName, columnName.length());
                Object columnValue = record.get(columnName);
                int columnSize = columnValue != null ? columnValue.toString().length() : "null".length();
                columnMaxSizes.computeIfPresent(
                        columnName,
                        (k, oldValue) -> columnSize > oldValue ? columnSize : oldValue
                );
            }
        }

        StringJoiner tableJoiner = new StringJoiner("\n");
        addHeader(columnNames, columnMaxSizes, tableJoiner);
        addRows(records, columnNames, columnMaxSizes, tableJoiner);
        return tableJoiner.toString();
    }

    private void addHeader(Set<String> columnNames, Map<String, Integer> columnMaxSizes, StringJoiner rowJoiner) {
        StringJoiner headerJoiner = new StringJoiner("|", "|", "|");
        for (String columnName : columnNames) {
            String cell = drawCell(columnName, columnMaxSizes.get(columnName));
            headerJoiner.add(cell);
        }
        rowJoiner.add(headerJoiner.toString());
    }

    private <T> void addRows(List<Map<String, T>> records,
                             Set<String> columnNames,
                             Map<String, Integer> columnMaxSizes,
                             StringJoiner tableJoiner) {
        for (Map<String, ?> record : records) {
            StringJoiner cellJoiner = new StringJoiner("|", "|", "|");
            for (String columnName : columnNames) {
                String cell = drawCell(record.get(columnName), columnMaxSizes.get(columnName));
                cellJoiner.add(cell);
            }
            tableJoiner.add(cellJoiner.toString());
        }
    }

    private String drawCell(Object object, int maxSize) {
        String value = object != null ? object.toString() : "null";
        return " " + StringUtils.rightPad(value, maxSize) + " ";
    }

}
