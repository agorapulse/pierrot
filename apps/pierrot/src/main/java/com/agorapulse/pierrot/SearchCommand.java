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
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "search",
    description = "searches GitHub and prints the matching files"
)
public class SearchCommand implements Runnable {

    private static final String LINE = "-".repeat(80);
    private static final String DOUBLE_LINE = "=".repeat(80);

    @Mixin SearchMixin search;

    @Option(
        names = {"-P", "--no-page"},
        description = "include archived repositories"
    )
    boolean noPage;

    @Inject GitHubService service;

    @Override
    public void run() {
        String query = search.getQuery();
        AtomicInteger found = new AtomicInteger();

        System.out.println(DOUBLE_LINE);
        System.out.printf("Finding search results for '%s'!%n", query);

        if (!noPage) {
            System.out.println("Hit ENTER to continue to the next result or run with '--no-page' option to print everything at once");
        }

        service.search(query).forEach(content -> {
            if (!search.isAll() && content.getRepository().isArchived()) {
                return;
            }

            found.incrementAndGet();

            System.out.println(DOUBLE_LINE);
            System.out.printf("| %s/%s%n", content.getRepository().getFullName(), content.getPath());
            System.out.println(DOUBLE_LINE);

            System.out.println(content.getTextContent());
            System.out.println(LINE);

            if (!noPage) {
                System.console().readLine();
            }
        });

        System.out.printf("Found %d results!%n", found.get());
    }
}
