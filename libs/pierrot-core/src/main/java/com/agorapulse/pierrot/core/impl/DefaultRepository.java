package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.Repository;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHContentBuilder;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class DefaultRepository implements Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRepository.class);

    private final GHRepository repository;
    private final GitHubConfiguration configuration;

    public DefaultRepository(GHRepository repository, GitHubConfiguration configuration) {
        this.repository = repository;
        this.configuration = configuration;
    }

    @Override
    public String getFullName() {
        return repository.getFullName();
    }

    @Override
    public boolean isArchived() {
        return repository.isArchived();
    }

    @Override
    public boolean createBranch(String name) {
        if (hasBranch(name)) {
            return false;
        }

        try {
            repository.createRef("refs/heads/" + name, getLastCommitSha());
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception creating branch " + name, e);
            return false;
        }
    }

    @Override
    public Optional<URL> createPullRequest(String branch, String title, String message) {
        try {
            return Optional.of(repository.createPullRequest(title, branch, getDefaultBranch(), message).getHtmlUrl());
        } catch (IOException e) {
            LOGGER.error("Exception creating pull request " + title, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean writeFile(String branch, String message, String path, String text) {
        try {
            GHContentBuilder builder = repository.createContent().branch(branch).path(path).content(text).message(message);
            getFile(branch, path).ifPresent(content -> builder.sha(content.getSha()));
            builder.commit();
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception writing file " + path, e);
            return false;
        }
    }

    private Optional<Content> getFile(String branch, String path) {
        try {
            GHContent fileContent = repository.getFileContent(path, branch);
            return Optional.of(new DefaultContent(fileContent, repository, configuration));
        } catch (IOException e) {
            LOGGER.error("Exception fetching file " + path, e);
            return Optional.empty();
        }
    }

    private String getDefaultBranch() {
        return repository.getDefaultBranch() == null ? configuration.getDefaultBranch() : repository.getDefaultBranch();
    }

    private boolean hasBranch(String name) {
        try {
            return repository.getBranches().containsKey(name);
        } catch (IOException e) {
            LOGGER.error("Exception checking presence of branch " + name, e);
            return false;
        }
    }

    private String getLastCommitSha() {
        return repository.listCommits().iterator().next().getSHA1();
    }

}
