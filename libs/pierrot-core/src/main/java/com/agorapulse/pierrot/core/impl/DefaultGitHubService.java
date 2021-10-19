package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Repository;
import jakarta.inject.Singleton;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class DefaultGitHubService implements GitHubService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGitHubService.class);

    private final GitHubConfiguration configuration;
    private final GitHub client;

    public DefaultGitHubService(GitHubConfiguration configuration, GitHub client) {
        this.configuration = configuration;
        this.client = client;
    }

    @Override
    public Stream<Content> search(String query) {
        return StreamSupport.stream(client.searchContent().q(query).list().spliterator(), false).map((GHContent content) ->
            new DefaultContent(content, content.getOwner(), configuration)
        );
    }

    @Override
    public Optional<Repository> getRepository(String repositoryFullName) {
        try {
            return Optional.of(client.getRepository(repositoryFullName)).map((GHRepository repository) -> new DefaultRepository(repository, configuration));
        } catch (IOException e) {
            LOGGER.error("Exception fetching repository " + repositoryFullName, e);
            return Optional.empty();
        }
    }
}
