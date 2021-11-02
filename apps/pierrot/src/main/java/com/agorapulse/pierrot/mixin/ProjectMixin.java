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
package com.agorapulse.pierrot.mixin;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Project;
import com.agorapulse.pierrot.core.PullRequest;
import io.micronaut.core.util.StringUtils;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;

public class ProjectMixin {

    private static final List<String> IN_PROGRESS_MERGEABLE_STATES = List.of(
        //The merge is blocked.
        "BLOCKED",
        //The merge commit cannot be cleanly created.
        "dirty",
        //The merge is blocked due to the pull request being a draft.
        "draft",
        //The state cannot currently be determined.
        "unknown",
        //Mergeable with non-passing commit status.
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
        defaultValue = "To do"
    )
    String todoColumn;

    @CommandLine.Option(
        names = {"--progress-column"},
        description = "The name of the 'In progress' column in the project",
        defaultValue = "In progress"
    )
    String progressColumn;

    @CommandLine.Option(
        names = {"--done-column"},
        description = "The name of the 'Done' column in the project",
        defaultValue = "Done"
    )
    String doneColumn;

    private Project board;

    public ProjectMixin() { }

    public ProjectMixin(String project, String todoColumn) {
        this.project = project;
        this.todoColumn = todoColumn;
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

    public Optional<Project> getProject() {
        return Optional.ofNullable(board);
    }

    private String getColumnNameForPullRequest(PullRequest pr) {
        if (pr.isMerged()) {
            return doneColumn;
        }

        if (IN_PROGRESS_MERGEABLE_STATES.contains(pr.getMergeableState())) {
            return progressColumn;
        }

        return todoColumn;
    }

}
