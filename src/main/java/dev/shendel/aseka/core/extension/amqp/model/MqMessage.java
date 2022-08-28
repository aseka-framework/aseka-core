package dev.shendel.aseka.core.extension.amqp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

@Data(staticConstructor = "of")
public class MqMessage {

    @EqualsAndHashCode.Include
    private final String uid = UUID.randomUUID().toString();

    private final String body;
    private final Map<String, String> headers;

}
