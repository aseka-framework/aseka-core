package dev.shendel.aseka.core.extension.wiremock;

import dev.shendel.aseka.core.api.Extension;

public interface WiremockExtension extends Extension {

    void init();

    void destroy();

    void clean();

    void register(String mappingSpecJson);

    void cleanRequestsJournal();

}
