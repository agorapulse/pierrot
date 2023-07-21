/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Vladimir Orany.
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
package com.agorapulse.pierrot.cli;

import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.cli.mixin.*;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.LinkedHashSet;
import java.util.Set;

@Command(
    name = "create",
    description = "creates a file in a matching repositories and creates PRs",
    mixinStandardHelpOptions = true
)
public class CreateCommand implements Runnable {

    @Mixin PullRequestMixin pullRequest;
    @Mixin SearchMixin search;
    @Mixin FileMixin file;
    @Mixin ProjectMixin project;
    @Mixin WorkspaceMixin workspace;

    @Inject GitHubService service;

    @Override
    public void run() {
        // init from the pierrot.yml
        workspace.asPullRequestSource().ifPresent(pullRequest::defaultsFrom);
        workspace.readProjectName().ifPresent(p -> project.setProjectName(p));

        final Set<String> processed = new LinkedHashSet<>();
        search.searchContent(service, found ->
            project.addToProject(service, pullRequest.createPullRequest(service, found.getRepository().getFullName(),
                    (r, branch, message) -> {
                        if (!processed.add(r.getFullName())) {
                            return false;
                        }

                        String path = file.readPath();
                        String content = file.readContent();

                        System.out.printf("Creating file %s/%s%n", r.getFullName(), path);

                        if (r.writeFile(branch, message, path, content)) {
                            System.out.printf("Created %s/%s%n", r.getFullName(), path);
                            return true;
                        }

                        return false;
                    }
                )
            ).map(pr -> SearchMixin.toSafeUri(pr.getHtmlUrl())));

        System.out.printf("Created %d files%n", pullRequest.getPullRequestsCreated());

        project.getProject().ifPresent(p -> {
            System.out.printf("Pull requests were added to the project %s at %s%n", p.getName(), p.getHttpUrl());
        });
    }

}
