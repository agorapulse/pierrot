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
package com.agorapulse.pierrot.hub4j;

import com.agorapulse.pierrot.api.GitHubConfiguration;
import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;

import java.io.IOException;

@Factory
public class DefaultGitHubFactory {

    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(DefaultGitHubFactory.class);

    @Bean
    @Singleton
    public GitHub gitHub(GitHubConfiguration configuration) throws IOException {
        if (StringUtils.isEmpty(configuration.getToken())) {
            try {
                return GitHub.connect();
            } catch (IOException e) {
                printMissingTokenMessage();
                return GitHub.connectAnonymously();
            }
        }

        return GitHub.connectUsingOAuth(configuration.getToken());
    }

    private void printMissingTokenMessage() {
        LOGGER.error("GitHub client is not authenticated. Please, set up your GitHub token");
        LOGGER.error("    GITHUB_TOKEN environment variable");
        LOGGER.error("  --github-token=<token> command line parameter");
        LOGGER.error("Alternatively, see other authentication options in the GitHub API docs:");
        LOGGER.error("\n    https://github-api.kohsuke.org\n");
    }

}
