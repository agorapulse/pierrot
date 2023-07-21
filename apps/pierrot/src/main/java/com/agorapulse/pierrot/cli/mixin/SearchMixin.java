/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Vladimir Orany.
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
package com.agorapulse.pierrot.cli.mixin;

import com.agorapulse.pierrot.api.Content;
import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.api.Ignorable;
import com.agorapulse.pierrot.api.PullRequest;
import io.micronaut.core.util.StringUtils;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SearchMixin {

    public static URI toSafeUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot convert " + url + " to URI");
        }
    }

    @Parameters(
        arity = "1",
        description = "The search term such as 'org:agorapulse filename:build.gradle'",
        paramLabel = "QUERY"
    )
    List<String> queries;

    @Option(
        names = {"-a", "--all"},
        description = "Include archived and closed"
    )
    boolean all;

    @Option(
        names = {"-g", "--global"},
        description = "Do not constrain search to current organization"
    )
    boolean global;

    @Option(
        names = {"-P", "--no-page"},
        description = "Do not wait after each result"
    )
    boolean noPage;

    private boolean proceedToNextResult = true;
    private int processed;

    public void searchContent(GitHubService service, Function<Content, Optional<URI>> action) {
        System.out.printf("Searching content for '%s'!%n", getQuery());
        service.searchContent(getQuery(), global)
            .filter(Predicate.not(this::isIgnored))
            .takeWhile(t -> this.shouldProceedToNextResult())
            .forEach(c -> paginate(action.apply(c)));
    }

    public void searchPullRequests(GitHubService service, Function<PullRequest, Optional<URI>> action) {
        System.out.printf("Searching pull requests for '%s'!%n", getQuery());
        service.searchPullRequests(getQuery(), !all, global)
            .filter(Predicate.not(this::isIgnored))
            .takeWhile(t -> this.shouldProceedToNextResult())
            .forEach(c -> paginate(action.apply(c)));
    }

    private String getQuery() {
        return queries.stream().map(q -> {
            if (q.contains(" ")) {
                // the query has been probably entered with quotes
                return "\"" + q + "\"";
            }
            return q;
        }).collect(Collectors.joining(" "));
    }

    private boolean shouldProceedToNextResult() {
        processed++;
        return proceedToNextResult;
    }

    private boolean isIgnored(Ignorable ignorable) {
        if (all) {
            return false;
        }
        return ignorable.canBeIgnored();
    }

    public int getProcessed() {
        return processed;
    }

    public void paginate(Optional<URI> maybeUri) {
        if (!isNoPage()) {
            String fullPrompt = "ENTER=continue, q=quit, a=all";

            if (maybeUri.isPresent()) {
                fullPrompt += ", o=open on GitHub";
            }

            fullPrompt += ": ";

            String nextLine = System.console().readLine(fullPrompt);
            if (StringUtils.isNotEmpty(nextLine)) {
                if (nextLine.contains("q")) {
                    proceedToNextResult = false;
                }
                if (nextLine.contains("a")) {
                    noPage = true;
                }
                if (nextLine.contains("o")) {
                    maybeUri.ifPresent(this::open);
                }
            }
        }
    }

    private boolean isNoPage() {
        if (noPage) {
            return true;
        }
        if (System.console() == null) {
            System.out.println("Running in non-interactive mode, all results will be printed. If you are running inside Docker, use -i option to enable pagination.");
            noPage = true;
            return true;
        }
        return noPage;
    }

    private void open(URI uri) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException ignored) {
                    // ignored
                }
            }
        } catch (UnsatisfiedLinkError e) {
            // running GraalVM
            try {
                Runtime.getRuntime().exec("open " + uri);
            } catch (IOException ex) {
                System.out.println("Cannot open URL automatically. Please, copy the link into your browser yourself:\n    " + uri);
            }
        }
    }
}
