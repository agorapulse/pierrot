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
package com.agorapulse.pierrot.core.impl.client

import com.agorapulse.testing.fixt.Fixt
import com.stehno.ersatz.ErsatzServer
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class GitHubHttpClientSpec extends Specification {

    private static final String OWNER = 'agorapulse'
    private static final String REPO = 'pierrot'
    private static final String SHA = '599bd0fb9af3cd3dfd041402664dcf211f595be6'

    @Shared Fixt fixt = Fixt.create(GitHubHttpClientSpec)

    @AutoCleanup ApplicationContext context
    @AutoCleanup ErsatzServer server = new ErsatzServer({
        reportToConsole true
        autoStart true
    })

    GitHubHttpClient client

    void setup() {
        server.expectations {
            get "/repos/$OWNER/$REPO/commits/$SHA/check-runs", {
                responds().body(fixt.readText('checkRuns.json'), GitHubHttpClient.GITHUB_V_3_JSON)
            }
        }.start()

        server.start()

        context = ApplicationContext.builder(
            'micronaut.http.services.github.url': server.httpUrl,
            'github.token': 'abcdef'
        ).build()

        context.start()

        client = context.getBean(GitHubHttpClient)
    }

    void 'fetch check runs'() {
        when:
            CheckRunsListResult runs = client.getCheckRuns(OWNER, REPO, SHA)
        then:
            runs.totalCount == 2
            runs.checkRuns
            runs.checkRuns.size() == 2

        when:
            CheckRunResult run = runs.checkRuns.first()

        then:
            run.name == 'Check'
            run.status == 'completed'
            run.conclusion == 'failure'
    }

}
