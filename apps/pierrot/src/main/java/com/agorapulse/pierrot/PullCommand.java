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
import java.util.Optional;

@Command(
    name = "pull",
    description = "searches GitHub and pulls the matching files locally"
)
public class PullCommand implements Runnable {

    @Mixin SearchMixin search;
    @Mixin WorkspaceMixin workspace;

    @Inject GitHubService service;

    @Override
    public void run() {
        search.searchContent(service, content -> {
            File location = new File(workspace.getWorkspace(), String.format("%s/%s", content.getRepository().getFullName(), content.getPath()));
            content.writeTo(location);
            System.out.printf("Fetched %s/%s%n", content.getRepository().getFullName(), content.getPath());
            return Optional.of(location.toURI());
            // TODO: paginate?
        });

        System.out.printf("Found %d results!%n", search.getProcessed());
    }
}
