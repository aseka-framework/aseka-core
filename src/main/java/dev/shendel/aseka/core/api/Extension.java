package dev.shendel.aseka.core.api;


public interface Extension extends Cleanable {

    void init();

    void destroy();

    @Override
    default void clean() {
    }

}
