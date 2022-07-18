package dev.shendel.aseka.core.extension.amqp.model;

import lombok.Data;

@Data
public class Binding {
    private String name;
    //TODO add properties - headers, exchange etc.
}
