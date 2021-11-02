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
package com.agorapulse.pierrot.core.impl.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.jackson.annotation.JacksonFeatures;

import static io.micronaut.core.annotation.TypeHint.AccessType.*;

@GitHub
@Client("github")
@JacksonFeatures(
    disabledDeserializationFeatures = DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
)
@TypeHint(
    typeNames = {
        "com.agorapulse.pierrot.core.impl.client.CheckRunResult",
        "com.agorapulse.pierrot.core.impl.client.CheckRunsListResult",
    },
    accessType = {
        ALL_PUBLIC,
        ALL_DECLARED_CONSTRUCTORS,
        ALL_PUBLIC_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
        ALL_PUBLIC_METHODS,
        ALL_PUBLIC_FIELDS
    }
)
public interface GitHubHttpClient {

    String GITHUB_V_3_JSON = "application/vnd.github.v3+json";

    @Get("/repos/{owner}/{repo}/commits/{sha}/check-runs")
    @Consumes(GITHUB_V_3_JSON)
    CheckRunsListResult getCheckRuns(String owner, String repo, String sha);

}
