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
class CreateCommandSpec extends AbstractCommandSpec {

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
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> false
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest1)
        getOwnerName() >> OWNER
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        canWrite() >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> true
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest2)
        getOwnerName() >> OWNER
    }

    Content content1 = Mock {
        getRepository() >> repository1
    }

    Content content2 = Mock {
        getRepository() >> repository2
    }

    Content content3 = Mock {
        getRepository() >> repository2
    }

    Project project = Mock {
        getName() >> PROJECT
        getHttpUrl() >> new URL("https://example.com/$OWNER/projects/1")
    }

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)

        searchContent(CONTENT_SEARCH_TERM, false) >> {
            Stream.of(content1, content2, content3)
        }

        findOrCreateProject(OWNER, PROJECT, 'In progress') >> Optional.of(project)
    }

    String command = 'create'

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
                String[] args = [
                    'create',
                    '-b',
                    BRANCH,
                    '-t',
                    TITLE,
                    '-m',
                    MESSAGE,
                    '-p',
                    PATH,
                    '-c',
                    CONTENT,
                    '--project',
                    PROJECT,
                    '-P',
                    CONTENT_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

        then:
            out == fixt.readText('create.txt')

            _ * pullRequest1.getRepository() >> repository1
            _ * pullRequest2.getRepository() >> repository2
    }

}
