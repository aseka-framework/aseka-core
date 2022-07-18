package dev.shendel.aseka.core.context;

import dev.shendel.aseka.core.api.Cleanable;

import java.util.Map;

public interface ContextVariables extends Cleanable {

    String get(String varName);

    Map<String, String> getAll();

    void set(String varName, Object value);

    void set(Map<String, ?> variables);

}
