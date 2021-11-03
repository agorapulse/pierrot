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
import com.agorapulse.pierrot.core.PullRequest
import com.agorapulse.pierrot.core.Repository
import com.agorapulse.testing.fixt.Fixt
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Stream

abstract class AbstractCommandSpec extends Specification {

    public static final String OWNER = 'agorapulse'
    public static final String CONTENT_SEARCH_TERM = 'org:agorapulse filename:.testfile'
    public static final String BRANCH = 'chore/test'
    public static final String TITLE = 'Test Title'
    public static final String MESSAGE = 'Test Message'
    public static final String CONTENT = 'Test Content'
    public static final String PATH = '.testfile'
    public static final String PROJECT = 'Pierrot'
    public static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    public static final String REPOSITORY_TWO = 'agorapulse/oss'
    public static final String PR_SEARCH_TERM = 'Agorapulse BOM'

    @Shared Fixt fixt = Fixt.create(getClass())
    @AutoCleanup ApplicationContext context

    abstract String getCommand()
    abstract GitHubService getService()

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
        canWrite() >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> true
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest1)
        getOwnerName() >> OWNER
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        canWrite() >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT.reverse()) >> false
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest2)
        getOwnerName() >> OWNER
    }

    void setup() {
        context = ApplicationContext.builder().build()
        context.registerSingleton(GitHubService, service)
        context.start()

        pullRequest1.getRepository() >> repository1
        pullRequest2.getRepository() >> repository2
    }

    void 'display help'() {
        when:
            String out = ConsoleOutput.capture {
                PicocliRunner.run(PierrotCommand, context, command, '--help')
            }.out
        then:
            // uncomment to rewrite the files
            // fixt.writeText('help.txt', out)
            out == fixt.readText('help.txt')
    }

}
