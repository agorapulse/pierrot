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

import com.agorapulse.pierrot.api.Project;
import com.agorapulse.pierrot.api.PullRequest;
import com.agorapulse.pierrot.api.event.PullRequestAddedToProjectEvent;
import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.kohsuke.github.GHProject;
import org.kohsuke.github.HttpException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DefaultProject implements Project {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(DefaultProject.class);

    private final GHProject project;
    private final ApplicationEventPublisher publisher;

    public DefaultProject(GHProject project, ApplicationEventPublisher publisher) {
        this.project = project;
        this.publisher = publisher;
    }

    @Override
    public void addToColumn(String column, PullRequest pr) {
        try {
            StreamSupport.stream(project.listColumns().spliterator(), false)
                .filter(col -> column.equals(col.getName()))
                .findFirst()
                .or(() -> {
                    try {
                        return Optional.of(project.createColumn(column));
                    } catch (IOException e) {
                        LOGGER.error(String.format("Exception creating column %s in project %s", column, project.getName()), e);
                        return Optional.empty();
                    }
                })
                .ifPresent(col -> {
                    if (pr instanceof DefaultPullRequest) {
                        try {
                            col.createCard(((DefaultPullRequest) pr).getNativePullRequest());
                            publisher.publishEvent(new PullRequestAddedToProjectEvent(this, pr));
                        } catch (HttpException e) {
                            if (e.getResponseCode() != 422) {
                                LOGGER.error(String.format("Exception while adding PR to the column %s in project %s", column, project.getName()), e);
                            }
                        } catch (IOException e) {
                            LOGGER.error(String.format("Exception while adding PR to the column %s in project %s", column, project.getName()), e);
                        }
                    } else {
                        LOGGER.error(String.format("Cannot add PR to the column %s in project %s - wrong type", column, project.getName()));
                    }
                });
        } catch (IOException e) {
            LOGGER.error(String.format("Exception adding PR to the column %s in project %s", column, project.getName()), e);
        }
    }

    @Override
    public void remove(PullRequest pr) {
        try {
            StreamSupport.stream(project.listColumns().spliterator(), false)
                .flatMap(col -> {
                    try {
                        return StreamSupport.stream(col.listCards().spliterator(), false);
                    } catch (IOException e) {
                        LOGGER.error(String.format("Cannot list project cards from column %s in %s", col, project.getName()), e);
                        return Stream.empty();
                    }
                })
                .filter(card -> {
                    try {
                        return card.getContent().getHtmlUrl().equals(pr.getHtmlUrl());
                    } catch (IOException e) {
                        LOGGER.error(String.format("Cannot fetch content of project card %d from %s", card.getId(), project.getName()), e);
                        return false;
                    }
                })
                .findFirst()
                .ifPresent(card -> {
                    try {
                        card.delete();
                    } catch (IOException e) {
                        LOGGER.error(String.format("Cannot remove project card %d from %s", card.getId(), project.getName()), e);
                    }
                });
        } catch (IOException e) {
            LOGGER.error(String.format("Exception removing PR %s from project %s", pr.getTitle(), project.getName()), e);
        }
    }

    @Override
    public URL getHttpUrl() {
        try {
            return project.getHtmlUrl();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return project.getName();
    }
}
