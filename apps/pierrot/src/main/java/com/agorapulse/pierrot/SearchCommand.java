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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@Command(
    name = "search",
    description = "searches GitHub and prints the matching files"
)
public class SearchCommand implements Runnable {

    private static final String LINE = "-".repeat(120);
    private static final String DOUBLE_LINE = "=".repeat(120);

    @Mixin SearchMixin search;

    @Inject GitHubService service;

    @Override
    public void run() {
        System.out.println(DOUBLE_LINE);
        System.out.printf("Searching results for '%s'!%n", search.getQuery());
        search.searchContent(service).forEach(content -> {
                System.out.println(DOUBLE_LINE);
                System.out.printf("| %s/%s%n", content.getRepository().getFullName(), content.getPath());
                System.out.println(DOUBLE_LINE);

                System.out.println(content.getTextContent());
                System.out.println(LINE);

                search.paginate("Hit ENTER to continue, 'q' for exit or 'o' to open on GitHub: ", answer -> {
                    if (answer.contains("o") && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(URI.create(content.getHtmlUrl()));
                        } catch (IOException ignored) {
                            // ignored
                        }
                    }
                });
            });

        System.out.printf("Found %d results!%n", search.getProcessed());
    }
}
