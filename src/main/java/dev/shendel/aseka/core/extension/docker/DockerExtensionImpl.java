package dev.shendel.aseka.core.extension.docker;

import com.google.common.base.Stopwatch;
import dev.shendel.aseka.core.configuration.DockerProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.service.FileManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
@Order(value = HIGHEST_PRECEDENCE)
public class DockerExtensionImpl implements DockerExtension {

    private final DockerProperties properties;
    private final FileManager fileManager;

    private DockerComposeContainer dockerCompose;

    @Override
    @SneakyThrows
    public void init() {
        if (properties.isEnabled()) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            log.info("Initializing Docker extension...");
            List<String> filePaths = properties.getComposeFiles();
            log.info("Starting docker containers for files:{}", filePaths);
            List<File> files = getDockerComposeFiles(filePaths);
            dockerCompose = new DockerComposeContainer(files).withLocalCompose(true);
            findAndRegisterWaitsForHealthchecks(files);

            dockerCompose.start();
            waitAfterStartIfNeeded();
            log.info("Docker containers started for {} seconds", stopwatch.elapsed(TimeUnit.SECONDS));
        }
    }

    private List<File> getDockerComposeFiles(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new AsekaException("Docker file paths can't be empty");
        }
        return paths.stream()
                .map(fileManager::getFile)
                .collect(Collectors.toList());
    }

    private void findAndRegisterWaitsForHealthchecks(List<File> files) {
        for (String serviceName : getServiceNamesWithHealthcheck(files)) {
            log.info("Docker will wait {} for healthcheck", serviceName);
            //TODO move to property
            dockerCompose.waitingFor(
                    serviceName,
                    Wait.forHealthcheck()
                            .withStartupTimeout(Duration.of(5, ChronoUnit.MINUTES))
            );
        }
    }

    private List<String> getServiceNamesWithHealthcheck(List<File> files) {
        return files.stream()
                .flatMap(file -> getServiceNamesWithHealthcheck(file).stream())
                .collect(Collectors.toList());
    }

    private List<String> getServiceNamesWithHealthcheck(File composeFile) {
        Map<String, Object> composeFileContent;

        try (FileInputStream fileInputStream = FileUtils.openInputStream(composeFile)) {
            Yaml yaml = new Yaml();
            composeFileContent = yaml.load(fileInputStream);
            Map<String, Object> services = (Map<String, Object>) composeFileContent.get("services");

            Set<String> serviceNames = services.keySet();
            log.info(
                    "Found {} services in docker-compose file: {}",
                    serviceNames.size(),
                    String.join(", ", serviceNames)
            );

            List<String> serviceNamesWithHealthCheck = services.entrySet().stream()
                    .filter(entry -> {
                        Map<String, Object> serviceParams = (Map<String, Object>) entry.getValue();
                        return serviceParams.containsKey("healthcheck");
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            log.info(
                    "Found {} services with healthcheck in docker-compose file: {}",
                    serviceNamesWithHealthCheck.size(),
                    String.join(", ", serviceNamesWithHealthCheck)
            );

            return serviceNamesWithHealthCheck;
        } catch (Exception e) {
            log.warn("Error when parsing services healthchecks", e);
            return new ArrayList<>();
        }
    }

    private void waitAfterStartIfNeeded() throws InterruptedException {
        Integer waitAfterStartSeconds = properties.getWaitAfterStartSeconds();
        if (waitAfterStartSeconds != null) {
            log.info("Waiting {} seconds", waitAfterStartSeconds);
            Thread.sleep(waitAfterStartSeconds * 1000);
        }
    }

    @Override
    public void destroy() {
        if (dockerCompose != null) {
            log.info("Destroying docker containers");
            dockerCompose.stop();
        }
    }

}
