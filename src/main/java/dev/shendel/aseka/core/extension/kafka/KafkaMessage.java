package dev.shendel.aseka.core.extension.kafka;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

@Data(staticConstructor = "of")
public class KafkaMessage {

    @EqualsAndHashCode.Include
    private final String uid = UUID.randomUUID().toString();

    private final String body;
    private final long offset;
    private final Map<String, String> headers;

}
