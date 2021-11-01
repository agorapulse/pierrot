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
package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.mixin.FileMixin;
import com.agorapulse.pierrot.mixin.PullRequestMixin;
import com.agorapulse.pierrot.mixin.SearchMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.LinkedHashSet;
import java.util.Set;

@Command(
    name = "create",
    description = "creates a file in a matching repositories and creates PRs"
)
public class CreateCommand implements Runnable {

    @Mixin PullRequestMixin pullRequest;
    @Mixin SearchMixin search;
    @Mixin FileMixin file;

    @Inject GitHubService service;

    @Override
    public void run() {
        final Set<String> processed = new LinkedHashSet<>();
        search.searchContent(service, found -> pullRequest.createPullRequest(service, found.getRepository().getFullName(), (r, branch, message) -> {
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
        }));

        System.out.printf("Created %d files%n", pullRequest.getPullRequestsCreated());
    }

}
