package dev.shendel.aseka.core.extension.amqp.model;

import lombok.Data;

@Data
public class Queue {
    private String name;
    private String exchange = "";
    //TODO add properties - headers, exchange etc.
}
