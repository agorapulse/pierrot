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

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

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

    public SearchMixin() { }

    public SearchMixin(List<String> queries, boolean all) {
        this.queries = queries;
        this.all = all;
    }

    public List<String> getQueries() {
        return queries;
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
}
