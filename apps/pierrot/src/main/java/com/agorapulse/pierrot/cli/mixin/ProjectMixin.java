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
package com.agorapulse.pierrot.cli.mixin;

import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.api.Project;
import com.agorapulse.pierrot.api.PullRequest;
import com.agorapulse.pierrot.api.source.ProjectSource;
import io.micronaut.core.util.StringUtils;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;

public class ProjectMixin implements ProjectSource {

    public static final String COLUMN_TO_DO = "To do";
    public static final String COLUMN_IN_PROGRESS = "In progress";
    public static final String COLUMN_DONE = "Done";

    private static final List<String> IN_PROGRESS_MERGEABLE_STATES = List.of(
        // The merge is blocked.
        "blocked",
        // The merge commit cannot be cleanly created.
        "dirty",
        // The merge is blocked due to the pull request being a draft.
        "draft",
        // The state cannot currently be determined.
        "unknown",
        // Mergeable with non-passing commit status.
        "unstable"
    );

    @CommandLine.Option(
        names = {"--project"},
        description = "The name of the project (board)"
    )
    String project;

    @CommandLine.Option(
        names = {"--todo-column"},
        description = "The name of the 'To do' column in the project",
        defaultValue = COLUMN_TO_DO
    )
    String todoColumn;

    @CommandLine.Option(
        names = {"--progress-column"},
        description = "The name of the 'In progress' column in the project",
        defaultValue = COLUMN_IN_PROGRESS
    )
    String progressColumn;

    @CommandLine.Option(
        names = {"--done-column"},
        description = "The name of the 'Done' column in the project",
        defaultValue = COLUMN_DONE
    )
    String doneColumn;

    private Project board;

    public void defaultsFrom(WorkspaceDescriptor other) {
        if (StringUtils.isEmpty(project)) {
            project = other.getProject();
        }

        if (StringUtils.isEmpty(todoColumn)) {
            todoColumn = other.getTodoColumn();
        }

        if (StringUtils.isEmpty(progressColumn)) {
            progressColumn = other.getProgressColumn();
        }

        if (StringUtils.isEmpty(doneColumn)) {
            doneColumn = other.getDoneColumn();
        }
    }

    public void storeInto(WorkspaceDescriptor other) {
        if (StringUtils.isNotEmpty(project)) {
            other.setProject(project);
        }

        if (StringUtils.isNotEmpty(todoColumn)) {
            other.setTodoColumn(todoColumn);
        }

        if (StringUtils.isNotEmpty(progressColumn)) {
            other.setProgressColumn(progressColumn);
        }

        if (StringUtils.isNotEmpty(doneColumn)) {
            other.setDoneColumn(doneColumn);
        }
    }

    public Optional<PullRequest> addToProject(GitHubService service, Optional<PullRequest> pullRequest) {
        if (StringUtils.isEmpty(project)) {
            // project must be specified to add projects
            return pullRequest;
        }
        return pullRequest.map(pr -> {
            String defaultOrganization = pr.getRepository().getOwnerName();
            String currentColumn = getColumnNameForPullRequest(pr);
            service.findOrCreateProject(defaultOrganization, project, currentColumn).ifPresent(p -> {
                p.addToColumn(currentColumn, pr);
                board = p;
            });
            return pr;
        });
    }

    public Optional<PullRequest> removeFromProject(GitHubService service, Optional<PullRequest> pullRequest) {
        if (StringUtils.isEmpty(project)) {
            // project must be specified to remove from project
            return pullRequest;
        }
        return pullRequest.map(pr -> {
            String defaultOrganization = pr.getRepository().getOwnerName();
            service.findProject(defaultOrganization, project).ifPresent(p -> {
                p.remove(pr);
                board = p;
            });
            return pr;
        });
    }

    @Override
    public Optional<Project> getProject() {
        return Optional.ofNullable(board);
    }

    @Override
    public String getColumnNameForPullRequest(PullRequest pr) {
        if (pr.isMerged()) {
            return doneColumn;
        }

        if (IN_PROGRESS_MERGEABLE_STATES.contains(pr.getMergeableState())) {
            return progressColumn;
        }

        return todoColumn;
    }

    @Override
    public boolean hasProject() {
        return StringUtils.isNotEmpty(project);
    }

    public void setProjectName(String project) {
        this.project = project;
    }

}
