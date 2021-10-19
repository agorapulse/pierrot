package com.agorapulse.pierrot.core.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LocalRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRepository.class);

    private final File location;
    private final String name;

    public LocalRepository(File location, String name) {
        this.location = location;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void visitFiles(Consumer<LocalFile> visitor) {
        try (Stream<Path> files = Files.find(location.toPath(), Integer.MAX_VALUE, (f, attrs) -> Files.isRegularFile(f))) {
            files.forEach(f -> {
                try {
                    String path = f.toFile().getCanonicalPath().substring(location.getCanonicalPath().length() + 1);
                    visitor.accept(new LocalFile(f.toFile(), path));
                } catch (IOException e) {
                    LOGGER.error("Exception extracting path from" + f, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Exception walking repository " + getName(), e);
        }
    }

}
