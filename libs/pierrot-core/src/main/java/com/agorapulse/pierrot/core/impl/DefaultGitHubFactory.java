package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.GitHubConfiguration;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.kohsuke.github.GitHub;

import java.io.IOException;

@Factory
public class DefaultGitHubFactory {

    @Bean
    @Singleton
    public GitHub gitHub(GitHubConfiguration configuration) throws IOException {
        return GitHub.connectUsingOAuth(configuration.getToken());
    }

}
