package dev.shendel.aseka.core.extension.db;

public enum SqlScriptType {
    SELECT,
    ANY;

    public static final String REGEX = "(ANY|SELECT)";

}
