package dev.shendel.aseka.core.extension.kafka;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Broker {

    private String name;
    private Map<String, String> properties = new HashMap<>();
    private List<Topic> topics = new ArrayList<>();

}
