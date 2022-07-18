package dev.shendel.aseka.core.service;

import java.util.Map;


public interface StringInterpolator {

    String interpolate(String input);

    Map<String, String> interpolate(Map<String, String> inputMap);

}
