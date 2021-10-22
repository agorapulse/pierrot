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
package com.agorapulse.pierrot.mixin;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Ignorable;
import io.micronaut.core.util.StringUtils;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SearchMixin {

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

    public SearchMixin() { }

    public SearchMixin(List<String> queries, boolean all, boolean global) {
        this.queries = queries;
        this.all = all;
        this.global = global;
    }

    public String getQuery() {
        return String.join(" ", queries);
    }

    public boolean isAll() {
        return all;
    }

    public boolean isGlobal() {
        return global;
    }

    public Stream<Content> searchContent(GitHubService service) {
        return service.searchContent(getQuery(), isGlobal())
            .filter(Predicate.not(this::isIgnored))
            .takeWhile(t -> this.shouldProceedToNextResult());
    }

    public boolean shouldProceedToNextResult() {
        processed++;
        return proceedToNextResult;
    }

    public boolean isIgnored(Ignorable ignorable) {
        if (all) {
            return false;
        }
        return ignorable.canBeIgnored();
    }

    public int getProcessed() {
        return processed;
    }

    public void paginate(String prompt, Consumer<String> lineConsumer) {
        if (!isNoPage()) {
            String nextLine = System.console().readLine(prompt);
            if (StringUtils.isNotEmpty(nextLine)) {
                if (nextLine.contains("q")) {
                    proceedToNextResult = false;
                }
                lineConsumer.accept(nextLine);
            }
        }
    }

    private boolean isNoPage() {
        if (System.console() == null) {
            System.out.println("Running in non-interactive mode, all results will be printed. If you are running inside Docker, use -i option to enable pagination.");
            noPage = true;
            return true;
        }
        return noPage;
    }
}
