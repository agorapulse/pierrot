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
package com.agorapulse.pierrot.cli;

import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.api.Repository;
import com.agorapulse.pierrot.api.ws.Workspace;
import com.agorapulse.pierrot.cli.mixin.SearchMixin;
import com.agorapulse.pierrot.cli.mixin.WorkspaceMixin;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Command(
    name = "pull",
    description = "pulls the matching files locally",
    mixinStandardHelpOptions = true
)
public class PullCommand implements Runnable {

    @Mixin SearchMixin search;
    @Mixin WorkspaceMixin workspace;

    @CommandLine.Option(
        names = {"-o", "--workspace-repositories-only"},
        description = "Search only within the existing workspace repositories"
    )
    boolean workspaceRepositoriesOnly;

    @Inject GitHubService service;

    @Override
    public void run() {
        List<String> repositoryFullNames = new ArrayList<>();
        if (workspaceRepositoriesOnly) {
            Workspace w = new Workspace(workspace.getWorkspace());
            w.visitRepositories(r -> repositoryFullNames.add(r.getName()));
        }

        search.searchContent(service, content -> {
            String repositoryFullName = content.getRepository().getFullName();

            if (!repositoryFullNames.isEmpty() && !repositoryFullNames.contains(repositoryFullName)) {
                System.out.printf("Ignoring %s/%s as the repository is not pulled%n", repositoryFullName, content.getPath());
                return Optional.empty();
            }

            Optional<Repository> maybeRepository = service.getRepository(repositoryFullName);

            if (maybeRepository.isEmpty()) {
                System.out.printf("Repository %s is not available.%n", repositoryFullName);
                return Optional.empty();
            }

            Repository ghr = maybeRepository.get();

            if (ghr.isArchived()) {
                System.out.printf("Repository %s is archived.%n", ghr.getFullName());
                return Optional.empty();
            }

            if (!ghr.canWrite()) {
                System.out.printf("Current user does not have write rights to the repository %s.%n", ghr.getFullName());
                return Optional.empty();
            }

            File location = new File(workspace.getWorkspace(), String.format("%s/%s", repositoryFullName, content.getPath()));
            content.writeTo(location);
            System.out.printf("Fetched %s/%s%n", repositoryFullName, content.getPath());
            return Optional.of(location.toURI());
        });

        System.out.printf("Found %d results!%n", search.getProcessed());
    }
}
