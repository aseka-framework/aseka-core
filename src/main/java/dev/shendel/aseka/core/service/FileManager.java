package dev.shendel.aseka.core.service;

import dev.shendel.aseka.core.api.Cleanable;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface FileManager extends Cleanable {

    String TEMP_FOLDER = "temp";

    File createFile(String path, InputStream inputStream);

    File createTextFile(String path, InputStream inputStream);

    File createTextFile(String path, String data);

    File getFile(String path);

    InputStream getFileInputStream(String path);

    String readFileAsString(String path);

    List<String> readFilesAsString(String filesLocationPattern);

}
