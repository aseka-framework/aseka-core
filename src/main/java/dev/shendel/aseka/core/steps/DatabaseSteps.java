package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.context.ContextVariables;
import dev.shendel.aseka.core.cucumber.parser.MatcherFactory;
import dev.shendel.aseka.core.cucumber.type.InterpolatedString;
import dev.shendel.aseka.core.cucumber.type.Pair;
import dev.shendel.aseka.core.extension.db.DatabaseExtension;
import dev.shendel.aseka.core.extension.db.SqlScriptType;
import dev.shendel.aseka.core.matcher.AsekaMatcher;
import dev.shendel.aseka.core.matcher.ContainsSqlRecords;
import dev.shendel.aseka.core.service.FileManager;
import dev.shendel.aseka.core.service.StringInterpolator;
import dev.shendel.aseka.core.util.Asserts;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.hamcrest.Matcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class DatabaseSteps {

    private final ContextVariables contextVariables;
    private final StringInterpolator stringInterpolator;
    private final FileManager fileManager;
    private final DatabaseExtension database;

    @Getter @Setter
    private String sqlScript;
    @Getter @Setter
    private List<Map<String, Object>> actualRecords;

    @When("set current db {interpolated_string}")
    public void setScenarioDataSource(String dataSourceName) {
        database.setScenarioDataSource(dataSourceName);
    }

    @When("execute {sql_script_type} SQL script {file_path}")
    public void executeQuery(SqlScriptType scriptType, String sqlFilePath) {
        sqlScript = fileManager.readFileAsString(sqlFilePath);
        Allure.addAttachment("Sql script", sqlScript);
        executeQuery(scriptType);
    }

    @When("execute {sql_script_type} SQL script:")
    public void executeQuery(SqlScriptType scriptType, InterpolatedString sqlScript) {
        this.sqlScript = sqlScript.get();
        executeQuery(scriptType);
    }

    @When("execute SQL script {file_path}")
    public void executeQuery(String sqlFilePath) {
        sqlScript = fileManager.readFileAsString(sqlFilePath);
        Allure.addAttachment("Sql script", sqlScript);
        executeQuery(SqlScriptType.ANY);
    }

    @When("execute SQL script:")
    public void executeQuery(InterpolatedString sqlScript) {
        this.sqlScript = sqlScript.get();
        executeQuery(SqlScriptType.ANY);
    }

    private void executeQuery(SqlScriptType method) {
        switch (method) {
            case SELECT:
                actualRecords = database.executeSelect(this.sqlScript);
                attachActualRecordsToAllure(actualRecords);
                break;
            case ANY:
                database.execute(this.sqlScript);
                break;
        }
    }

    @Then("check that response records count {matcher} {int}")
    @SuppressWarnings({"rawtypes"})
    public void checkResponseSize(AsekaMatcher asekaMatcher, Integer expectedRecordsNumber) {
        Matcher matcher = MatcherFactory.create(asekaMatcher, expectedRecordsNumber);
        Asserts.assertThat(
                actualRecords.size(),
                matcher,
                "Wrong sql records count. Expected: {} {}, Actual: {}",
                asekaMatcher.getName(),
                expectedRecordsNumber,
                actualRecords.size()
        );
    }

    @Then("check response record(s):")
    public void checkResponseRecords(List<Map<String, String>> expectedRecords) {
        expectedRecords = expectedRecords.stream()
                .map(interpolateRecords())
                .collect(toList());
        Asserts.assertThat(actualRecords, ContainsSqlRecords.containsSqlRecords(expectedRecords));
    }

    private Function<Map<String, String>, Map<String, String>> interpolateRecords() {
        return map -> map.entrySet()
                .stream()
                .collect(
                        LinkedHashMap::new,
                        (newMap, entry) -> newMap.put(
                                stringInterpolator.interpolate(entry.getKey()),
                                stringInterpolator.interpolate(entry.getValue())
                        ),
                        LinkedHashMap::putAll
                );
    }

    @When("get variables from {int} row in response:")
    public void setVariables(int sqlRowNumber, List<Pair> rows) {
        Asserts.assertThat(actualRecords.size() >= sqlRowNumber, "response don't have {} row", sqlRowNumber);

        for (Pair pair : rows) {
            String variableName = pair.getFirst();
            String dbColumnName = pair.getSecond();
            Object value = actualRecords.get(sqlRowNumber - 1).get(dbColumnName);
            contextVariables.set(variableName, value);
        }
    }

    public static void attachActualRecordsToAllure(List<Map<String, Object>> rowsInTable) {
        if (rowsInTable == null || rowsInTable.isEmpty()) {
            Allure.addAttachment("Actual records", "empty");
        } else {
            Allure.addAttachment("Actual records", "text/tab-separated-values", convertToCsv(rowsInTable), "csv");
        }
    }

    private static String convertToCsv(List<Map<String, Object>> rowsInTable) {
        final StringBuilder dataTableCsv = new StringBuilder();
        for (int i = 0; i < rowsInTable.size(); i++) {
            Set<Map.Entry<String, Object>> entries = rowsInTable.get(i).entrySet();
            List<Map.Entry<String, Object>> row = Lists.newArrayList(entries.iterator());

            if (!row.isEmpty()) {
                if (i == 0) {
                    addRow(dataTableCsv, row.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
                    dataTableCsv.append('\n');
                }
                addRow(dataTableCsv, row.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
                dataTableCsv.append('\n');
            }

        }
        return dataTableCsv.toString();
    }

    private static void addRow(StringBuilder dataTableCsv, List<Object> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i == columns.size() - 1) {
                dataTableCsv.append(columns.get(i));
            } else {
                dataTableCsv.append(columns.get(i));
                dataTableCsv.append('\t');
            }
        }
    }

}