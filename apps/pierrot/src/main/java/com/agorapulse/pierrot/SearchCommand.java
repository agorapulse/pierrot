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
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "search",
    description = "searches GitHub and prints the matching files"
)
public class SearchCommand implements Runnable {

    private static final String LINE = "-".repeat(120);
    private static final String DOUBLE_LINE = "=".repeat(120);

    @Mixin SearchMixin search;

    @Option(
        names = {"-P", "--no-page"},
        description = "Do not wait after each result"
    )
    boolean noPage;

    @Inject GitHubService service;

    @Override
    public void run() {
        if (System.console() == null) {
            System.out.println("Running in non-interactive mode, all results will be printed. If you are running inside Docker, use -i option to enable pagination.");
            noPage = true;
        }

        String query = search.getQuery();
        AtomicInteger found = new AtomicInteger();
        Scanner scanner = new Scanner(System.in);

        System.out.println(DOUBLE_LINE);
        System.out.printf("Searching results for '%s'!%n", query);

        if (!noPage) {
            System.out.println("The results will be paginated. Use '--no-page' option to print everything at once.");
        }

        AtomicBoolean proceedToNextResult = new AtomicBoolean(true);

        service.searchContent(query, search.isGlobal()).takeWhile(c -> proceedToNextResult.get()).forEach(content -> {
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
                System.out.print("Hit ENTER to continue, 'q' for exit or 'o' to open on GitHub: ");
                String nextLine = scanner.nextLine();
                if (StringUtils.isNotEmpty(nextLine)) {
                    if (nextLine.contains("q")) {
                        proceedToNextResult.set(false);
                    }
                    if (nextLine.contains("o")) {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            try {
                                Desktop.getDesktop().browse(URI.create(content.getHtmlUrl()));
                            } catch (IOException ignored) {
                                // ignored
                            }
                        }
                    }
                }
            }
        });

        System.out.printf("Found %d results!%n", found.get());
    }
}
