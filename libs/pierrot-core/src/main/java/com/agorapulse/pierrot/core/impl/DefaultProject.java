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

import com.agorapulse.pierrot.core.Project;
import com.agorapulse.pierrot.core.PullRequest;
import com.agorapulse.pierrot.core.util.LoggerWithOptionalStacktrace;
import org.kohsuke.github.GHProject;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class DefaultProject implements Project {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(DefaultProject.class);

    private final GHProject project;

    public DefaultProject(GHProject project) {
        this.project = project;
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
                        LOGGER.error("Exception creating column " + column + " in project " + project.getName(), e);
                        return Optional.empty();
                    }
                })
                .ifPresent(col -> {
                    if (pr instanceof DefaultPullRequest) {
                        try {
                            col.createCard(((DefaultPullRequest) pr).getNativePullRequest());
                        } catch (IOException e) {
                            LOGGER.error("Exception adding PR to the column " + column + " in project " + project.getName(), e);
                        }
                    } else {
                        LOGGER.error("Cannot add PR to the column " + column + " in project " + project.getName() + " - wrong type");
                    }
                });
        } catch (IOException e) {
            LOGGER.error("Exception adding PR to the column " + column + " in project " + project.getName(), e);
        }
    }
}
