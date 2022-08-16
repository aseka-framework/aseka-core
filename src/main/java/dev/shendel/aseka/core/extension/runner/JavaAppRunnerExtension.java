package dev.shendel.aseka.core.extension.runner;

import dev.shendel.aseka.core.api.Extension;
import dev.shendel.aseka.core.configuration.JavaAppRunnerProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.exception.ExceptionWrapper;
import dev.shendel.aseka.core.service.RetryExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.shendel.aseka.core.util.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@Service
@Order(value = Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class JavaAppRunnerExtension implements Extension {

    @Getter
    private final Queue<String> appLogsQueue = new ConcurrentLinkedQueue<>();
    private final JavaAppRunnerProperties properties;

    private Process javaProcess;

    @Override
    @SneakyThrows
    public void init() {
        if (properties.isEnabled()) {
            startApp();
            startListenLogs();
            waitUntilHealthcheckPass();
        }
    }

    @SneakyThrows
    private void waitUntilHealthcheckPass() {
        AtomicBoolean appStarted = new AtomicBoolean(false);
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            RetryExecutor.of(properties.getHealthCheckTimeoutSeconds())
                    .retryAnyException()
                    .execute(() -> {
                        try {
                            HttpResponse response = client.execute(new HttpGet(properties.getHealthCheckUrl()));
                            int statusCode = response.getStatusLine().getStatusCode();
                            assertThat(statusCode, equalTo(HttpStatus.SC_OK));
                            appStarted.set(true);
                        } catch (IOException e) {
                            ExceptionWrapper.sneakyThrow(e);
                        }
                    });
        } catch (IOException e) {
            //ignore
        }
        if (appStarted.get()) {
            log.info("App started");
        } else {
            throw new AsekaException("App not started properly. Healthcheck don't have 200 status");
        }
    }

    @Override
    public void destroy() {
        javaProcess.destroy();
    }

    @SneakyThrows
    //TODO add java args properties
    public void startApp() {
        log.info("Starting java app");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().putAll(properties.getEnvironment());
        processBuilder.command(
                buildJavaCommand(),
                "-XX:NativeMemoryTracking=detail",
                "-jar",
                properties.getAppPath()
        );
        buildAppProperties().forEach(
                prop -> processBuilder.command().add(prop)
        );

        log.info("Executing command: {}", String.join(" ", processBuilder.command()));
        javaProcess = processBuilder.start();
    }

    private String buildJavaCommand() {
        String javaPath = properties.getJavaPath();
        if (StringUtils.isNotBlank(javaPath)) {
            javaPath = javaPath.replaceAll("/$", "");
            return javaPath + "/java";
        } else {
            return "java";
        }
    }

    private List<String> buildAppProperties() {
        return properties.getAppProperties()
                .entrySet()
                .stream()
                .map(property -> "--" + property.getKey() + "=" + property.getValue())
                .collect(Collectors.toList());
    }

    private void startListenLogs() {
        Executors.newSingleThreadExecutor().submit(() -> listenLogs(javaProcess));
    }

    @SneakyThrows
    private void listenLogs(Process process) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            appLogsQueue.add(line);
            if (properties.isAppLoggingEnabled()) {
                log.info("[APP LOG] {}", line);
            }
        }
    }

}
