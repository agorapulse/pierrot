package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.Repository;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DefaultContent implements Content {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContent.class);

    private final GHContent content;
    private final Repository repositoryWrapper;

    public DefaultContent(GHContent content, GHRepository repository, GHUser myself, GitHubConfiguration configuration) {
        this.content = content;
        this.repositoryWrapper = new DefaultRepository(repository, myself, configuration);
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
    public boolean delete(String branchName, String message) {
        try {
            content.delete(message, branchName);
            return true;
        } catch (GHFileNotFoundException e) {
            LOGGER.info("File {}/{} no longer exists", getRepository().getFullName(), getPath());
            return false;
        } catch (IOException e) {
            LOGGER.error("Exception deleting " + getRepository().getFullName() + "/" + getPath(), e);
            return false;
        }
    }

    @Override
    public boolean replace(String branchName, String message, String regexp, String replacement) {
        try {
            String text = getTextContent();
            String newText = text.replaceAll(regexp, replacement);

            if (newText.equals(text)) {
                LOGGER.info("The content of {} is still the same after replacement", getPath());
                return false;
            }

            content.update(newText, message, branchName);
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception updating " + getRepository().getFullName() + "/" + getPath(), e);
        }
        return false;
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
