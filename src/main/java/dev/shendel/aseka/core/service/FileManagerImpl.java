package dev.shendel.aseka.core.service;

import com.google.common.collect.Lists;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.shendel.aseka.core.util.Validator.checkThat;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@RequiredArgsConstructor
public final class FileManagerImpl implements FileManager {

    private static final List<String> SUPPORTED_TEXT_FILES = Lists.newArrayList(
            "txt",
            "json",
            "csv",
            "sql",
            "xml",
            "xsd",
            "html",
            "yaml"
    );

    private final StringInterpolator stringInterpolator;
    private final ResourcePatternResolver loader = new PathMatchingResourcePatternResolver();

    @Override
    public File createFile(String path, InputStream inputStream) {
        Validator.checkDownloadFolder(path, TEMP_FOLDER);
        try {
            File file = innerCreateFile(path);
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file;
        } catch (IOException e) {
            throw new AsekaException("Can't create file", e);
        }
    }

    @SneakyThrows
    @Override
    public File createTextFile(String path, InputStream inputStream) {
        String data = IOUtils.toString(inputStream, UTF_8);
        return createTextFile(path, data);
    }

    @Override
    public File createTextFile(String path, String data) {
        Validator.checkDownloadFolder(path, TEMP_FOLDER);
        try {
            File textFile = innerCreateFile(path);
            data = stringInterpolator.interpolate(data);
            FileUtils.writeStringToFile(textFile, data, UTF_8);
            return textFile;
        } catch (IOException e) {
            throw new AsekaException("Can't create file", e);
        }
    }

    private File innerCreateFile(String path) throws IOException {
        File file = new File(path);
        Validator.checkThat(!file.exists(), "File already exists:'{}'", path);
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }

    @SneakyThrows
    @Override
    public File getFile(String path) {
        return loader.getResource(path).getFile();
    }

    @Override
    public InputStream getFileInputStream(String filePath) {
        try {
            Resource file = loader.getResource(filePath);
            if (isTextFile(file)) {
                String content = readAsResolvedString(file);
                return IOUtils.toInputStream(content, UTF_8);
            } else {
                return file.getInputStream();
            }
        } catch (IOException e) {
            throw new AsekaException("Can't read file:'{}'", e, filePath);
        }
    }

    @Override
    public String readFileAsString(String filePath) {
        Resource resource = loader.getResource(filePath);
        return readAsResolvedString(resource);
    }

    @Override
    public List<String> readFilesAsString(String filesLocationPattern) {
        List<String> fileContentList = new ArrayList<>();
        try {
            for (Resource resource : loader.getResources(filesLocationPattern)) {
                String resolvedContent = readAsResolvedString(resource);
                fileContentList.add(resolvedContent);
            }
            return fileContentList;
        } catch (IOException e) {
            throw new AsekaException("Can't read file(s):'{}'", e, filesLocationPattern);
        }
    }

    private String readAsResolvedString(Resource resource) {
        try (
                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), UTF_8);
                BufferedReader bufReader = new BufferedReader(reader)
        ) {
            Validator.checkThat(
                    isTextFile(resource),
                    "Don't support text file '{}'. Supported files: {}",
                    getFileExtension(resource),
                    SUPPORTED_TEXT_FILES
            );

            String content = bufReader.lines().collect(Collectors.joining(System.lineSeparator()));
            return stringInterpolator.interpolate(content);
        } catch (IOException e) {
            throw new AsekaException("Can't read file:'{}'", e, resource.getDescription());
        }
    }

    private boolean isTextFile(Resource resource) {
        try {
            String fileExtension = getFileExtension(resource);
            return SUPPORTED_TEXT_FILES.contains(fileExtension);
        } catch (IOException e) {
            throw new AsekaException("Can't read file:'{}'", resource.getDescription());
        }
    }

    private String getFileExtension(Resource resource) throws IOException {
        String fileName = resource.getFile().getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    @Override
    public void clean() {
        File tempDir = new File(TEMP_FOLDER);
        FileUtils.deleteQuietly(tempDir);
        checkThat(!tempDir.exists(), "Error deleting temporary files from:'{}'", TEMP_FOLDER);
        log.info("Temporary files deleted from:{}", TEMP_FOLDER);
    }

}
