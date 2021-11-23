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
package com.agorapulse.pierrot.cli;

import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.cli.mixin.ProjectMixin;
import com.agorapulse.pierrot.cli.mixin.PullRequestMixin;
import com.agorapulse.pierrot.cli.mixin.SearchMixin;
import com.agorapulse.pierrot.cli.mixin.WorkspaceMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "delete",
    description = "creates PRs to delete files",
    mixinStandardHelpOptions = true
)
public class DeleteCommand implements Runnable {

    @Mixin SearchMixin search;
    @Mixin PullRequestMixin pullRequest;
    @Mixin ProjectMixin project;
    @Mixin WorkspaceMixin workspace;

    @Inject GitHubService service;

    @Override
    public void run() {
        // init from the pierrot.yml
        workspace.asPullRequestSource().ifPresent(pullRequest::defaultsFrom);
        workspace.readProjectName().ifPresent(p -> project.setProjectName(p));

        search.searchContent(service, content -> project.addToProject(service, pullRequest.createPullRequest(service, content.getRepository().getFullName(), (r, branch, message) -> {
            System.out.printf("Deleting %s/%s%n", content.getRepository().getFullName(), content.getPath());

            if (content.delete(branch, message)) {
                System.out.printf("Deleted %s/%s%n", content.getRepository().getFullName(), content.getPath());
                return true;
            }

            return false;
        })).map(pr -> SearchMixin.toSafeUri(pr.getHtmlUrl())));

        System.out.printf("Processed %d files%n", pullRequest.getPullRequestsCreated());
    }

}
