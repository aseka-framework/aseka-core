package dev.shendel.aseka.core.extension.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DatabaseType {
    ORACLE("oracle.jdbc.OracleDriver"),
    POSTGRES("org.postgresql.Driver"),
    SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver");

    private final String driverName;

}
