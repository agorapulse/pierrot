/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Vladimir Orany.
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
package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Repository;
import jakarta.inject.Singleton;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
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

    private GHMyself myself;

    public DefaultGitHubService(GitHubConfiguration configuration, GitHub client) {
        this.configuration = configuration;
        this.client = client;
    }

    @Override
    public Stream<Content> search(String query) {
        return StreamSupport.stream(client.searchContent().q(query).list().spliterator(), false).map((GHContent content) ->
            new DefaultContent(content, content.getOwner(), getMyself(), configuration)
        );
    }

    @Override
    public Optional<Repository> getRepository(String repositoryFullName) {
        try {
            return Optional.of(client.getRepository(repositoryFullName)).map((GHRepository repository) -> new DefaultRepository(repository, getMyself(), configuration));
        } catch (IOException e) {
            LOGGER.error("Exception fetching repository " + repositoryFullName, e);
            return Optional.empty();
        }
    }

    private GHUser getMyself() {
        if (myself != null) {
            return myself;
        }
        try {
            this.myself = client.getMyself();
            return myself;
        } catch (IOException e) {
            LOGGER.error("Exception fetching current user ", e);
            return new GHUser();
        }
    }
}
