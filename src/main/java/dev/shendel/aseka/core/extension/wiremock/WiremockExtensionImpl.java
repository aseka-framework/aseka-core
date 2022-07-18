package dev.shendel.aseka.core.extension.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import dev.shendel.aseka.core.configuration.WiremockProperties;
import dev.shendel.aseka.core.service.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.create;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class WiremockExtensionImpl implements WiremockExtension {
    //TODO add readable error message when extension isn't enabled

    private final WiremockProperties properties;
    private final FileManager fileManager;

    private WireMock client;
    private WireMockServer server;

    @Override
    public void init() {
        if (isExtensionEnabled()) {
            log.info("Initializing Wiremock extension...");
            if (isEmbeddedServerEnabled()) {
                log.info("Starting embedded wiremock server...");
                server = new WireMockServer(options().port(properties.getPort()));
                server.start();
            }
            client = create().host(properties.getHost()).port(properties.getPort()).build();
            WireMock.configureFor(client);
            if (hasDefaultMocks()) {
                log.info("Found default mocks");
                registerDefaultMocks();
            }
        }
    }

    @Override
    public void register(String mappingSpecJson) {
        StubMapping stubMapping = StubMapping.buildFrom(mappingSpecJson);
        client.register(stubMapping);
    }

    @Override
    public void cleanRequestsJournal() {
        client.resetRequests();
    }

    @Override
    public void clean() {
        if (isExtensionEnabled()) {
            client.resetMappings();
            registerDefaultMocks();
            log.info("Mocks successfully reset to defaults");
        }
    }

    @Override
    public void destroy() {
        if (isEmbeddedServerEnabled()) {
            server.stop();
            log.info("Embedded wiremock server stopped");
        }
    }

    private boolean isExtensionEnabled() {
        return properties.isEnabled();
    }

    private boolean isEmbeddedServerEnabled() {
        return properties.isEmbeddedServerEnabled();
    }

    private void registerDefaultMocks() {
        if (hasDefaultMocks()) {
            List<String> mappingSpecJson = fileManager.readFilesAsString(properties.getDefaultMocksPath());
            mappingSpecJson.forEach(this::register);
        }
    }

    private boolean hasDefaultMocks() {
        return isNotBlank(properties.getDefaultMocksPath());
    }

}
