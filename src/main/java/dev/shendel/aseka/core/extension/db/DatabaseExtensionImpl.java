package dev.shendel.aseka.core.extension.db;

import dev.shendel.aseka.core.configuration.DatabaseProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static dev.shendel.aseka.core.util.Validator.checkThat;
import static dev.shendel.aseka.core.util.Validator.checkThatAllNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseExtensionImpl implements DatabaseExtension {

    private final DatabaseProperties properties;

    private final Map<String, DataSource> dataSources = new HashMap<>();
    private String defaultDataSourceName;
    private String currentDataSourceName;

    @Override
    public void init() {
        if (properties.isEnabled()) {
            checkThat(!properties.getDataSources().isEmpty(), "DataSources is empty.");
            checkThatDefaultDataSourceIsSet();

            properties.getDataSources().forEach(dataSourceProperty -> {
                dataSources.put(dataSourceProperty.getName(), buildDataSource(dataSourceProperty));
                if (dataSourceProperty.isDefault()) {
                    defaultDataSourceName = dataSourceProperty.getName();
                }

            });
            currentDataSourceName = defaultDataSourceName;
            log.info("Default database is: {}", defaultDataSourceName);
        }
    }

    private void checkThatDefaultDataSourceIsSet() {
        List<DatabaseProperties.DataSource> defaultDataSources = properties.getDataSources()
                .stream()
                .filter(DatabaseProperties.DataSource::isDefault)
                .collect(Collectors.toList());

        checkThat(defaultDataSources.size() == 1, "Only and at least 1 dataSource must be declared");
    }

    private DataSource buildDataSource(DatabaseProperties.DataSource dataSourceProperty) {
        String url = dataSourceProperty.getUrl();
        String user = dataSourceProperty.getUser();
        String password = dataSourceProperty.getPassword();
        DatabaseType type = dataSourceProperty.getType();

        checkThat(type != null, "DataSource type must be declared");
        checkThatAllNotBlank("Wrong database settings", url, user, password);

        return DataSourceBuilder.create()
                .driverClassName(type.getDriverName())
                .url(url)
                .username(user)
                .password(password)
                .build();
    }

    @Override
    public void setScenarioDataSource(String dataSourceName) {
        checkThat(dataSources.containsKey(dataSourceName), "DataSource `{}` not found", dataSourceName);
        currentDataSourceName = dataSourceName;
        log.info("DataSource '{}' was set in scenario.", dataSourceName);
    }

    @Override
    @SneakyThrows
    public List<Map<String, Object>> executeSelect(String sqlScript) {
        log.info("Executing SQL script: \n{}", sqlScript);

        DataSource currentDataSource = getCurrentDataSource();
        List<String> queries = parseStatements(currentDataSource, sqlScript);
        checkThat(queries.size() == 1, "Script must content only 1 query");

        QueryRunner run = new QueryRunner(currentDataSource);
        return run.query(sqlScript, new MapListHandler());
    }

    @Override
    @SneakyThrows
    public void execute(String sqlScript) {
        log.info("Executing SQL script: \n{}", sqlScript);

        DataSource currentDataSource = getCurrentDataSource();

        List<String> queries = parseStatements(currentDataSource, sqlScript);
        checkThat(queries.size() > 0, "Script must content at least 1 query");

        QueryRunner runner = new QueryRunner(currentDataSource);
        queries.forEach(query -> {
            try {
                runner.execute(query);
            } catch (SQLException e) {
                throw new AsekaException("Error during execution query: \n{}", e, query);
            }
        });
    }

    @SneakyThrows
    private static List<String> parseStatements(DataSource dataSource, String sqlScript) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            SqlScript script = DatabaseTypeRegister.getDatabaseTypeForConnection(connection)
                    .createSqlScriptFactory(Flyway.configure().dataSource(dataSource), new ParsingContext())
                    .createSqlScript(new StringResource(sqlScript), true, null);
            return Lists.newArrayList(script.getSqlStatements())
                    .stream()
                    .map(SqlStatement::getSql)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void destroy() {
        clean();
    }

    @Override
    public void clean() {
        currentDataSourceName = defaultDataSourceName;
    }

    private DataSource getCurrentDataSource() {
        return dataSources.get(currentDataSourceName);
    }

}
