package com.agorapulse.pierrot.core.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LocalFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFile.class);

    private final File location;
    private final String path;

    public LocalFile(File location, String path) {
        this.location = location;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        try {
            return Files.readString(location.toPath());
        } catch (IOException e) {
            LOGGER.error("Exception reading text content of the file " + getPath(), e);
            return "";
        }
    }

}
