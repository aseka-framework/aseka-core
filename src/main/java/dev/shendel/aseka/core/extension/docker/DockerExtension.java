package dev.shendel.aseka.core.extension.docker;

import dev.shendel.aseka.core.api.Extension;

public interface DockerExtension extends Extension {
    void init();

    void destroy();

    @Override
    default void clean() {
    }

}
