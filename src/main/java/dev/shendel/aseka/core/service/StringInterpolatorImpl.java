package dev.shendel.aseka.core.service;

import dev.shendel.aseka.core.api.InterpolationHelper;
import dev.shendel.aseka.core.api.InterpolatorHelpersSupplier;
import dev.shendel.aseka.core.context.ContextVariables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class StringInterpolatorImpl implements StringInterpolator {

    public static final String NULL_PLACEHOLDER = "${{null}}";
    public static final String EMPTY_STRING_PLACEHOLDER_PROPERTY = "variables.empty=";
    public static final String EMPTY_STRING_PLACEHOLDER = "${{empty}}";
    public static final String PREFIX = "${{";
    public static final String SUFFIX = "}}";

    private final ContextVariables contextVariables;
    private final List<InterpolatorHelpersSupplier> interpolatorHelpersSuppliers;

    private StringSubstitutor delegate;

    @PostConstruct
    public void init() {
        List<InterpolationHelper> helpers = interpolatorHelpersSuppliers.stream()
                .flatMap(supplier -> supplier.get().stream())
                .collect(Collectors.toList());

        StringLookup variablesLookup = StringLookupFactory.INSTANCE.functionStringLookup(contextVariables::get);
        StringLookup finalLookup = StringLookupFactory.INSTANCE.interpolatorStringLookup(
                toStringLookups(helpers),
                variablesLookup,
                false
        );

        delegate = new StringSubstitutor(finalLookup)
                .setEnableUndefinedVariableException(true)
                .setEnableSubstitutionInVariables(true)
                .setVariablePrefix(PREFIX)
                .setVariableSuffix(SUFFIX);
    }

    private Map<String, StringLookup> toStringLookups(List<InterpolationHelper> helpers) {
        return helpers.stream()
                .collect(
                        Collectors.toMap(
                                InterpolationHelper::getName,
                                helper -> StringLookupFactory.INSTANCE.functionStringLookup(helper.getFunction())
                        )
                );
    }

    @Override
    public String interpolate(String input) {
        if (NULL_PLACEHOLDER.equals(input)) {
            return null;
        }
        return delegate.replace(input);
    }

    @Override
    public Map<String, String> interpolate(Map<String, String> inputMap) {
        return inputMap.entrySet()
                .stream()
                .collect(
                        HashMap::new,
                        (map, entry) -> map.put(interpolate(entry.getKey()), interpolate(entry.getValue())),
                        HashMap::putAll
                );
    }

}
