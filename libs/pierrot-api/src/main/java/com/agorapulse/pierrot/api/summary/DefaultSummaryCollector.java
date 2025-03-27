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
package com.agorapulse.pierrot.api.summary;

import com.agorapulse.pierrot.api.Project;
import com.agorapulse.pierrot.api.PullRequest;
import com.agorapulse.pierrot.api.Repository;
import com.agorapulse.pierrot.api.event.*;
import io.micronaut.context.annotation.Context;
import io.micronaut.runtime.event.annotation.EventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Context
public class DefaultSummaryCollector implements SummaryCollector {

    private Project project;
    private final Set<Repository> updatedRepositories = new HashSet<>();
    private final Map<String, PullRequest> createdPullRequests = new HashMap<>();
    private final Map<String, List<ContentUpdatedEvent>> updatedContents = new HashMap<>();

    @EventListener
    public void onContentUpdated(ContentUpdatedEvent event) {
        updatedRepositories.add(event.getContent().getRepository());
        updatedContents.computeIfAbsent(event.getContent().getRepository().getFullName(), k -> new ArrayList<>()).add(event);
    }

    @EventListener
    public void onProjectCreated(ProjectCreatedEvent event) {
        this.project = event.getProject();
    }

    @EventListener
    public void onPullRequestAddedToProject(PullRequestAddedToProjectEvent event) {
        this.project = event.getProject();
    }

    @EventListener
    public void onPullRequestCreated(PullRequestCreatedEvent event) {
        updatedRepositories.add(event.getPullRequest().getRepository());
        createdPullRequests.put(event.getPullRequest().getRepository().getFullName(), event.getPullRequest());
    }

    @Override
    public String getSummary() {
        StringBuilder builder = new StringBuilder("### Pierrot Summary\n\n");

        if (project != null) {
            builder.append("Project: [").append(project.getName()).append("](").append(project.getHttpUrl()).append(")\n\n");
        }

        if (!updatedRepositories.isEmpty()) {
            updatedRepositories.stream().sorted(Comparator.comparing(Repository::getFullName)).forEach(r -> {
                builder.append("#### [").append(r.getFullName()).append("](").append("https://github.com/").append(r.getFullName()).append(")\n\n");
                if (createdPullRequests.containsKey(r.getFullName())) {
                    PullRequest pr = createdPullRequests.get(r.getFullName());
                    builder.append("New pull request: [").append(pr.getTitle()).append("](").append(pr.getHtmlUrl()).append(")\n\n");
                }
                builder.append("|    | File |\n");
                builder.append("| --- | --- |\n");
                if (updatedContents.containsKey(r.getFullName())) {
                    List<ContentUpdatedEvent> events = updatedContents.get(r.getFullName());
                    if (!events.isEmpty()) {
                        for (ContentUpdatedEvent event : events) {
                            builder.append("| ").append(getIconForType(event.getUpdateType())).append(" | [").append(event.getContent().getPath()).append("](").append(event.getContent().getHtmlUrl()).append(") |\n");
                        }
                        builder.append("\n");
                    }
                }
            });
        } else {
            builder.append("No changes detected.\n\n");
        }

        return builder.toString();
    }

    private String getIconForType(UpdateType updateType) {
        switch (updateType) {
            case CREATED:
                return ":new:";
            case UPDATED:
                return ":pencil2:";
            case DELETED:
                return ":x:";
            default:
                return ":question:";
        }
    }

}
