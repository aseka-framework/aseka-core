package dev.shendel.aseka.core.extension.docker;

import dev.shendel.aseka.core.configuration.DockerProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.service.FileManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.util.List;
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
            log.info("Initializing Docker extension...");
            List<String> filePaths = properties.getComposeFiles();
            log.info("Starting docker containers for files:{}", filePaths);
            List<File> files = getDockerComposeFiles(filePaths);
            dockerCompose = new DockerComposeContainer(files)
                    .withLocalCompose(true);
            dockerCompose.start();

            log.info("Waiting {} ms", properties.getPostStartDelay());
            Thread.sleep(properties.getPostStartDelay());
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

    @Override
    public void destroy() {
        if (dockerCompose != null) {
            log.info("Destroying docker containers");
            dockerCompose.stop();
        }
    }

}
