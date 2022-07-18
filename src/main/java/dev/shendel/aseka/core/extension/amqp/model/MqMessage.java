package dev.shendel.aseka.core.extension.amqp.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data(staticConstructor = "of")
public class MqMessage {
    private final String body;
    private final Map<String, String> headers;

    public static MqMessage empty() {
        return new MqMessage(null, new HashMap<>());
    }

}
