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

import com.agorapulse.pierrot.core.CheckRun;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.PullRequest;
import com.agorapulse.pierrot.core.Repository;
import com.agorapulse.pierrot.core.util.LoggerWithOptionalStacktrace;
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

    public DefaultPullRequest(GHPullRequest pr, GHRepository repository, GHUser myself, GitHubConfiguration configuration) {
        this.pr = pr;
        this.repository = repository;
        this.myself = myself;
        this.configuration = configuration;
    }

    @Override
    public Repository getRepository() {
        return new DefaultRepository(repository, myself, configuration);
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

    GHPullRequest getNativePullRequest() {
        return pr;
    }
}
