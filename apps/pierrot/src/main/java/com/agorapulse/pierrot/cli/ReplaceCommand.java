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
package com.agorapulse.pierrot.cli;

import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.cli.mixin.ProjectMixin;
import com.agorapulse.pierrot.cli.mixin.PullRequestMixin;
import com.agorapulse.pierrot.cli.mixin.SearchMixin;
import com.agorapulse.pierrot.cli.mixin.WorkspaceMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
    name = "replace",
    description = "replaces content in the files and creates PRs",
    mixinStandardHelpOptions = true
)
public class ReplaceCommand implements Runnable {

    @Mixin SearchMixin search;
    @Mixin PullRequestMixin pullRequest;
    @Mixin ProjectMixin project;
    @Mixin WorkspaceMixin workspace;

    @Inject GitHubService service;

    @Option(
        names = {"-p", "--pattern"},
        description = "The Java-style regular expression pattern to execute on the matched files",
        required = true
    )
    String pattern;

    @Option(
        names = {"-r", "--replacement"},
        description = "The Java-style regular expression replacement",
        required = true
    )
    String replacement;

    @Override
    public void run() {
        // init from the pierrot.yml
        workspace.asPullRequestSource().ifPresent(pullRequest::defaultsFrom);
        workspace.readProjectName().ifPresent(p -> project.setProjectName(p));

        search.searchContent(service, content -> project.addToProject(service, pullRequest.createPullRequest(service, content.getRepository().getFullName(), (repository, branch, message) -> {
            System.out.printf("Replacing %s/%s%n", content.getRepository().getFullName(), content.getPath());

            if (content.replace(branch, message, pattern, replacement)) {
                System.out.printf("Replaced %s/%s%n", content.getRepository().getFullName(), content.getPath());
                return true;
            }

            return false;
        })).map(pr -> SearchMixin.toSafeUri(pr.getHtmlUrl())));

        System.out.printf("Replaced text in %d files%n", pullRequest.getPullRequestsCreated());
    }

}
