/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2022 Vladimir Orany.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.pierrot.hub4j;

import com.agorapulse.pierrot.api.GitHubConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Value;

import java.util.List;
import java.util.Optional;

@ConfigurationProperties("pierrot")
public class DefaultGitHubConfiguration implements GitHubConfiguration {

    private String token;
    private String defaultBranch = "master";
    private String organization;
    private List<String> projectColumns = List.of("To do", "In progress", "Done");

    public DefaultGitHubConfiguration(
        // compatibility with other tools
        @Value("${github.token}") Optional<String> githubToken,
        @Value("${github.oauth}") Optional<String> githubOauth
    ) {
        token = githubToken.or(() -> githubOauth).orElse(null);
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    @Override
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Override
    public List<String> getProjectColumns() {
        return projectColumns;
    }

    public void setProjectColumns(List<String> projectColumns) {
        this.projectColumns = projectColumns;
    }
}
