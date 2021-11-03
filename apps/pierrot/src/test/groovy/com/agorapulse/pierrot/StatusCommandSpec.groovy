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

import com.agorapulse.pierrot.core.CheckRun
import com.agorapulse.pierrot.core.GitHubService
import com.agorapulse.pierrot.core.Project
import com.agorapulse.pierrot.core.PullRequest
import com.agorapulse.pierrot.core.Repository
import io.micronaut.configuration.picocli.PicocliRunner

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class StatusCommandSpec extends AbstractCommandSpec {

    private static final String OWNER = 'agorapulse'
    private static final String SEARCH_TERM = 'Agorapulse BOM'
    private static final String PROJECT = 'Pierrot'
    private static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    private static final String REPOSITORY_TWO = 'agorapulse/oss'

    CheckRun run1 = Mock {
        getName() >> 'Check 1'
        getStatus() >> 'completed'
        getConclusion() >> 'success'
    }

    CheckRun run2 = Mock {
        getName() >> 'Check 2'
        getStatus() >> 'completed'
        getConclusion() >> 'failure'
    }

    CheckRun run3 =  Mock {
        getName() >> 'Check 3'
        getStatus() >> 'completed'
        getConclusion() >> 'unknown'
    }

    CheckRun run4 =  Mock {
        getName() >> 'Check 4'
        getStatus() >> 'pending'
    }

    PullRequest pullRequest1 = Mock {
        getTitle() >> 'Test PR 1'
        isMerged() >> true
        getMergeableState() >> 'unknown'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_ONE/pulls/1")
        getChecks() >> Stream.of(run1, run2)
    }

    PullRequest pullRequest2 = Mock {
        getTitle() >> 'Test PR 2'
        getMergeableState() >> 'unstable'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_TWO/pulls/1")
        getChecks() >> Stream.of(run3, run4)
    }

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
        getOwnerName() >> OWNER
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        getOwnerName() >> OWNER
    }

    Project project = Mock {
        getName() >> PROJECT
        getHttpUrl() >> new URL("https://example.com/$OWNER/projects/1")
    }

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)

        searchPullRequests(SEARCH_TERM, true, false) >> {
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
                    SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

        then:
            out == fixt.readText('status.txt')

            _ * pullRequest1.getRepository() >> repository1
            _ * pullRequest2.getRepository() >> repository2
    }

}
