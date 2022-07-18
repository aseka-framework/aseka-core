package dev.shendel.aseka.core.extension.kafka;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Topic {

    private Map<String,String> properties = new HashMap<>();
    private String name;

}
