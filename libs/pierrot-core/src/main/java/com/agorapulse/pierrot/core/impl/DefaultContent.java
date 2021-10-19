package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.Repository;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class DefaultContent implements Content {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContent.class);

    private final GHContent content;
    private final Repository repositoryWrapper;

    public DefaultContent(GHContent content, GHRepository repository, GitHubConfiguration configuration) {
        this.content = content;
        this.repositoryWrapper = new DefaultRepository(repository, configuration);
    }

    @Override
    public String getName() {
        return content.getName();
    }

    @Override
    public String getPath() {
        return content.getPath();
    }

    @Override
    public Repository getRepository() {
        return repositoryWrapper;
    }

    @Override
    public InputStream getContent() {
        try {
            return content.read();
        } catch (IOException e) {
            LOGGER.error("Exception fetching content of " + getRepository().getFullName() + "/" + getPath(), e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public String getTextContent() {
        try {
            return new String(getContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Exception fetching text content of " + getRepository().getFullName() + "/" + getPath(), e);
            return "";
        }
    }

    @Override
    public String getSha() {
        return content.getSha();
    }

    @Override
    public void delete(String branchName, String message) {
        try {
            content.delete(branchName, message);
        } catch (IOException e) {
            LOGGER.error("Exception deleting " + getRepository().getFullName() + "/" + getPath(), e);
        }
    }

    @Override
    public void update(String branchName, String message, String regexp, String replacement) {
        try {
            content.update(getTextContent().replaceAll(regexp, replacement), message, branchName);
        } catch (IOException e) {
            LOGGER.error("Exception updating " + getRepository().getFullName() + "/" + getPath(), e);
        }
    }

    @Override
    public void writeTo(File location) {
        location.getParentFile().mkdirs();

        try (OutputStream outStream = new FileOutputStream(location)) {
            outStream.write(getContent().readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
