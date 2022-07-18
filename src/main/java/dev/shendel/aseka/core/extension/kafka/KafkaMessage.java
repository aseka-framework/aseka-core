package dev.shendel.aseka.core.extension.kafka;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data(staticConstructor = "of")
public class KafkaMessage {
    private final String body;
    private final long offset;
    private final Map<String, String> headers;

    public static KafkaMessage empty() {
        return new KafkaMessage(null, 0, new HashMap<>());
    }

}
