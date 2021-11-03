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

import com.agorapulse.pierrot.core.Content
import com.agorapulse.pierrot.core.GitHubService
import com.agorapulse.pierrot.core.Project
import com.agorapulse.pierrot.core.PullRequest
import com.agorapulse.pierrot.core.Repository
import io.micronaut.configuration.picocli.PicocliRunner

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class ReplaceCommandSpec extends AbstractCommandSpec {

    private static final String OWNER = 'agorapulse'
    private static final String SEARCH_TERM = 'org:agorapulse filename:.testfile'
    private static final String BRANCH = 'chore/test'
    private static final String TITLE = 'Test Title'
    private static final String MESSAGE = 'Test Message'
    private static final String REPLACEMENT = 'salut $1'
    private static final String PATTERN = /hello (\w+)/
    private static final String PROJECT = 'Pierrot'
    private static final String PATH = '.testfile'
    private static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    private static final String REPOSITORY_TWO = 'agorapulse/oss'

    PullRequest pullRequest1 = Mock {
        getMergeableState() >> 'unstable'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_ONE/pulls/1")
    }

    PullRequest pullRequest2 = Mock {
        getMergeableState() >> 'unstable'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_TWO/pulls/1")
    }

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
        canWrite() >> true
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest1)
        getOwnerName() >> OWNER
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        canWrite() >> true
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest2)
        getOwnerName() >> OWNER
    }

    Content content1 = Mock {
        getRepository() >> repository1
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> true
        getPath() >> PATH
    }

    Content content2 = Mock {
        getRepository() >> repository2
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> true
        getPath() >> PATH
    }

    Content content3 = Mock {
        getRepository() >> repository2
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> false
        getPath() >> PATH
    }

    Project project = Mock {
        getName() >> PROJECT
        getHttpUrl() >> new URL("https://example.com/$OWNER/projects/1")
    }

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)

        searchContent(SEARCH_TERM, false) >> {
            Stream.of(content1, content2, content3)
        }

        findOrCreateProject(OWNER, PROJECT, 'In progress') >> Optional.of(project)
    }

    String command = 'replace'

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
                String[] args = [
                    'replace',
                    '-b',
                    BRANCH,
                    '-t',
                    TITLE,
                    '-m',
                    MESSAGE,
                    '-p',
                    PATTERN,
                    '-r',
                    REPLACEMENT,
                    '--project',
                    PROJECT,
                    '-P',
                    SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

        then:
            out == fixt.readText('replace.txt')

            _ * pullRequest1.getRepository() >> repository1
            _ * pullRequest2.getRepository() >> repository2
    }

}
