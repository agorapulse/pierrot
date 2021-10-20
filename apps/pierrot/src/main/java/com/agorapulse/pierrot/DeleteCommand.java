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
import com.agorapulse.pierrot.core.Repository;
import com.agorapulse.pierrot.mixin.PullRequestMixin;
import com.agorapulse.pierrot.mixin.SearchMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "delete",
    description = "searches GitHub and creates PRs to delete files"
)
public class DeleteCommand implements Runnable {

    @Mixin SearchMixin search;
    @Mixin PullRequestMixin pullRequest;

    @Inject GitHubService service;

    @Override
    public void run() {
        String query = search.getQuery();
        AtomicInteger deleted = new AtomicInteger();

        System.out.printf("Finding search results to delete for '%s'!%n", query);

        service.search(query).forEach(content -> {
            if (!search.isAll() && content.getRepository().isArchived()) {
                return;
            }

            Repository ghr = service.getRepository(content.getRepository().getFullName()).get();

            if (ghr.isArchived()) {
                System.out.printf("Repository %s is archived. Nothing will be deleted.%n", ghr.getFullName());
                return;
            }

            if (!ghr.canWrite()) {
                System.out.printf("Current user does not have write rights to the repository %s. Nothing will be deleted.%n", ghr.getFullName());
                return;
            }

            System.out.printf("Deleting %s/%s%n", content.getRepository().getFullName(), content.getPath());

            ghr.createBranch(pullRequest.readBranch());

            if (content.delete(pullRequest.readBranch(), pullRequest.readMessage())) {
                System.out.printf("Deleted %s/%s%n", content.getRepository().getFullName(), content.getPath());
                deleted.incrementAndGet();

                ghr.createPullRequest(pullRequest.readBranch(), pullRequest.readTitle(), pullRequest.readMessage()).ifPresent(url ->
                    System.out.printf("PR for %s available at %s%n", ghr.getFullName(), url)
                );
            }
        });

        System.out.printf("Deleted %d files%n", deleted.get());
    }

}
