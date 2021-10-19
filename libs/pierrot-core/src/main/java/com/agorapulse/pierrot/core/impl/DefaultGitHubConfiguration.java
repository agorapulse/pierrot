package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.GitHubConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("github")
public class DefaultGitHubConfiguration implements GitHubConfiguration {

    private String token;
    private String defaultBranch = "master";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }
}
