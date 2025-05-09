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
import com.agorapulse.pierrot.cli.mixin.SearchMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.net.URI;

import static java.util.Optional.of;

@Command(
    name = "search",
    description = "searches GitHub and prints the matching files",
    mixinStandardHelpOptions = true
)
public class SearchCommand implements Runnable {

    private static final String LINE = "-".repeat(120);
    private static final String DOUBLE_LINE = "=".repeat(120);

    @Mixin SearchMixin search;

    @Inject GitHubService service;

    @Override
    public void run() {
        search.searchContent(service, content -> {
            System.out.println(DOUBLE_LINE);
            System.out.printf("| %s/%s%n", content.getRepository().getFullName(), content.getPath());
            System.out.println(DOUBLE_LINE);

            System.out.println(content.getTextContent());
            System.out.println(LINE);

            return of(URI.create(content.getHtmlUrl()));
        });

        System.out.printf("Found %d results!%n", search.getProcessed());
    }
}
