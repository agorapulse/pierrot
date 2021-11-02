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
import com.agorapulse.testing.fixt.Fixt
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.stream.Stream

class CreateCommandSpec extends Specification {

    private static final String OWNER = 'agorapulse'
    private static final String SEARCH_TERM = 'org:agorapulse filename:.testfile'
    private static final String BRANCH = 'chore/test'
    private static final String TITLE = 'Test Title'
    private static final String MESSAGE = 'Test Message'
    private static final String CONTENT = 'Test Content'
    private static final String PATH = '.testfile'
    private static final String PROJECT = 'Pierrot'
    private static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    private static final String REPOSITORY_TWO = 'agorapulse/oss'

    @AutoCleanup ApplicationContext context

    Fixt fixt = Fixt.create(CreateCommandSpec)

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

        searchContent(SEARCH_TERM, false) >> {
            Stream.of(content1, content2, content3)
        }

        findOrCreateProject(OWNER, PROJECT, 'In progress') >> Optional.of(project)
    }

    void setup() {
        context = ApplicationContext.builder().build()
        context.registerSingleton(GitHubService, service)
        context.start()
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects', 'FactoryMethodName'])
    void 'create remote file'() {
        when:
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            System.out = new PrintStream(baos)

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
                SEARCH_TERM,
            ] as String[]
            PicocliRunner.run(PierrotCommand, context, args)

        then:
            baos.toString() == fixt.readText('create.txt')

            _ * pullRequest1.getRepository() >> repository1
            _ * pullRequest2.getRepository() >> repository2
    }

}
