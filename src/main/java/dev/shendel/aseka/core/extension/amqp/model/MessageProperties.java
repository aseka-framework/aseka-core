package dev.shendel.aseka.core.extension.amqp.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Data
public class MessageProperties {

    private String contentType;
    private String contentEncoding;
    private Map<String, String> headers;
    private Integer deliveryMode;
    private Integer priority;
    private String correlationId;
    private String replyTo;
    private String expiration;
    private String messageId;
    private Date timestamp;
    private String type;
    private String userId;
    private String appId;
    private String clusterId;

    public boolean isEmpty() {
        return Objects.isNull(contentType)
                && Objects.isNull(contentEncoding)
                && Objects.isNull(headers)
                && Objects.isNull(deliveryMode)
                && Objects.isNull(priority)
                && Objects.isNull(correlationId)
                && Objects.isNull(replyTo)
                && Objects.isNull(expiration)
                && Objects.isNull(messageId)
                && Objects.isNull(timestamp)
                && Objects.isNull(type)
                && Objects.isNull(userId)
                && Objects.isNull(appId)
                && Objects.isNull(clusterId);
    }

}
