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
package com.agorapulse.pierrot

import com.agorapulse.pierrot.core.GitHubService
import com.agorapulse.pierrot.core.Project
import io.micronaut.configuration.picocli.PicocliRunner

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class StatusCommandSpec extends AbstractCommandSpec {

    Project project = Mock {
        getName() >> PROJECT
        getHttpUrl() >> new URL("https://example.com/$OWNER/projects/1")
    }

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)

        searchPullRequests(PR_SEARCH_TERM, true, false) >> {
            Stream.of(pullRequest1, pullRequest2)
        }

        findOrCreateProject(OWNER, PROJECT, _ as String) >> Optional.of(project)
    }

    String command = 'status'

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
                String[] args = [
                    'status',
                    '--project',
                    PROJECT,
                    '-P',
                    PR_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

        then:
            out == fixt.readText('status.txt')
    }

}
