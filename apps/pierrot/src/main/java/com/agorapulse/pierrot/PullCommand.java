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
import com.agorapulse.pierrot.mixin.SearchMixin;
import com.agorapulse.pierrot.mixin.WorkspaceMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "pull",
    description = "searches GitHub and pulls the matching files locally"
)
public class PullCommand implements Runnable {

    private static final String DOUBLE_LINE = "=".repeat(80);

    @Mixin SearchMixin search;
    @Mixin WorkspaceMixin workspace;

    @Inject GitHubService service;

    @Override
    public void run() {
        String query = search.getQuery();
        AtomicInteger found = new AtomicInteger();

        System.out.println(DOUBLE_LINE);
        System.out.printf("Finding search results for '%s'!%n", query);
        service.search(query).forEach(content -> {
            if (!search.isAll() && content.getRepository().isArchived()) {
                return;
            }

            found.incrementAndGet();

            File location = new File(workspace.getWorkspace(), String.format("%s/%s", content.getRepository().getFullName(), content.getPath()));
            content.writeTo(location);
            System.out.printf("Fetched %s/%s%n", content.getRepository().getFullName(), content.getPath());
        });

        System.out.printf("Found %d results!%n", found.get());
    }
}
