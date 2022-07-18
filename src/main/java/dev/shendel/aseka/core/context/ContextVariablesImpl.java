package dev.shendel.aseka.core.context;

import dev.shendel.aseka.core.configuration.AsekaProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class ContextVariablesImpl implements ContextVariables {

    private final AsekaProperties properties;
    private final Map<String, String> contextVariables = new HashMap<>();

    @Override
    public String get(String varName) {
        if (contextVariables.containsKey(varName)) {
            return contextVariables.get(varName);
        } else if (properties.getDefaultVariables().containsKey(varName)) {
            return properties.getDefaultVariables().get(varName);
        } else {
            log.warn("Variable '{}' not found", varName);
            return null;
        }
    }

    @Override
    public Map<String, String> getAll() {
        Map<String, String> resultMap = new HashMap<>(properties.getDefaultVariables());
        resultMap.putAll(contextVariables);
        return resultMap;
    }

    @Override
    public void set(String varName, Object value) {
        if (value == null) {
            log.warn("Variable {} is null", varName);
            contextVariables.put(varName, null);
        } else {
            contextVariables.put(varName, value.toString());
        }
    }

    @Override
    public void set(Map<String, ?> variables) {
        variables.forEach(this::set);
    }

    @Override
    public void clean() {
        contextVariables.clear();
        log.info("Context variables successfully reset to defaults");
    }

}
