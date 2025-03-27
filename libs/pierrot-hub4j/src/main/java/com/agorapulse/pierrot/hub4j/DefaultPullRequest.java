/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Vladimir Orany.
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

import com.agorapulse.pierrot.api.CheckRun;
import com.agorapulse.pierrot.api.GitHubConfiguration;
import com.agorapulse.pierrot.api.PullRequest;
import com.agorapulse.pierrot.api.Repository;
import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DefaultPullRequest implements PullRequest {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(DefaultPullRequest.class);

    private final GHPullRequest pr;
    private final GHRepository repository;
    private final GHUser myself;
    private final GitHubConfiguration configuration;
    private final HttpClient client;
    private final ApplicationEventPublisher publisher;

    public DefaultPullRequest(GHPullRequest pr, GHRepository repository, GHUser myself, GitHubConfiguration configuration, HttpClient client, ApplicationEventPublisher publisher) {
        this.pr = pr;
        this.repository = repository;
        this.myself = myself;
        this.configuration = configuration;
        this.client = client;
        this.publisher = publisher;
    }

    @Override
    public Repository getRepository() {
        return new DefaultRepository(repository, myself, configuration, client, publisher);
    }

    @Override
    public String getTitle() {
        return pr.getTitle();
    }

    @Override
    public String getBody() {
        return pr.getBody();
    }

    @Override
    public boolean isMerged() {
        try {
            return pr.isMerged();
        } catch (IOException e) {
            LOGGER.error("Exception fetching merged state", e);
            return false;
        }
    }

    @Override
    public boolean isMergeable() {
        try {
            return Boolean.TRUE.equals(pr.getMergeable());
        } catch (IOException e) {
            LOGGER.error("Exception fetching mergeable state", e);
            return false;
        }
    }

    @Override
    public String getMergeableState() {
        try {
            return pr.getMergeableState();
        } catch (IOException e) {
            LOGGER.error("Exception fetching mergeable state", e);
            return "unknown";
        }
    }

    @Override
    public Stream<? extends CheckRun> getChecks() {
        try {
            return StreamSupport.stream(repository.getCommit(pr.getHead().getSha()).getCheckRuns().spliterator(), false).map(DefaultCheckRun::new);
        } catch (IOException e) {
            LOGGER.error("Exception fetching check runs", e);
            return Stream.empty();
        }
    }

    @Override
    public URL getHtmlUrl() {
        return pr.getHtmlUrl();
    }

    @Override
    public boolean close(boolean delete) {
        String repoName = pr.getRepository().getFullName();

        if (GHIssueState.CLOSED.equals(pr.getState())) {
            return true;
        }

        try {
            pr.close();
            LOGGER.info("Closed #{}/{}: {}", repoName, pr.getId(), pr.getTitle());
        } catch (IOException e) {
            LOGGER.error(String.format("Exception closing pull request for #%d: %s", pr.getId(), pr.getTitle()), e);
            return false;
        }

        if (!delete) {
            return false;
        }

        String branchName = pr.getHead().getRef();

        if (StringUtils.isEmpty(configuration.getToken())) {
            LOGGER.error("Missing token, cannot delete pull request branch {} for #{}: {}", branchName, pr.getId(), pr.getTitle());
            return false;
        }

        MutableHttpRequest<Object> request = HttpRequest
            .DELETE("https://api.github.com/repos/" + repoName + "/git/refs/heads/" + branchName)
            .header("User-Agent", "Pierrot")
            .header("Authorization", "token " + configuration.getToken());

        try {
            client.toBlocking().exchange(request);
            LOGGER.info("Deleted branch {} in {}", branchName, repoName);
            return true;
        } catch (HttpClientResponseException e) {
            LOGGER.error(String.format("Cannot delete pull request branch %s for #%s: %s", branchName, pr.getId(), pr.getTitle()), e);
            return false;
        }
    }

    GHPullRequest getNativePullRequest() {
        return pr;
    }
}
