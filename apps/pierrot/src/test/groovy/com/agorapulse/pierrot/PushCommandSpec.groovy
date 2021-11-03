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
import com.agorapulse.pierrot.core.PullRequest
import com.agorapulse.pierrot.core.Repository
import com.agorapulse.testing.fixt.Fixt
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.TempDir

@SuppressWarnings('UnnecessaryGetter')
class PushCommandSpec extends Specification {

    private static final String OWNER = 'agorapulse'
    private static final String BRANCH = 'chore/test'
    private static final String TITLE = 'Test Title'
    private static final String MESSAGE = 'Test Message'
    private static final String PATH = '.testfile'
    private static final String CONTENT = 'Test Content'
    private static final String PROJECT = 'Pierrot'
    private static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    private static final String REPOSITORY_TWO = 'agorapulse/oss'

    @AutoCleanup ApplicationContext context

    @TempDir File workspace

    Fixt fixt = Fixt.create(PushCommandSpec)

    PullRequest pullRequest1 = Mock {
        getMergeableState() >> 'unknown'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_ONE/pulls/1")
    }

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
        canWrite() >> true
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest1)
        getOwnerName() >> OWNER
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> true
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        canWrite() >> true
        getOwnerName() >> OWNER
        writeFile(BRANCH, MESSAGE, PATH, CONTENT.reverse()) >> false
    }

    Project project = Mock {
        getName() >> PROJECT
        getHttpUrl() >> new URL("https://example.com/$OWNER/projects/1")
    }

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)
        findOrCreateProject(OWNER, PROJECT, _ as String) >> Optional.of(project)
    }

    void setup() {
        context = ApplicationContext.builder().build()
        context.registerSingleton(GitHubService, service)
        context.start()

        createWorkspaceFile(REPOSITORY_ONE, PATH, CONTENT)
        createWorkspaceFile(REPOSITORY_TWO, PATH, CONTENT.reverse())
    }

    void 'run command'() {
        when:
            String out = ConsoleCapture.capture {
                String[] args = [
                    'push',
                    '-b',
                    BRANCH,
                    '-t',
                    TITLE,
                    '-m',
                    MESSAGE,
                    '--project',
                    PROJECT,
                    '-w',
                    workspace.canonicalPath,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }

        then:
            out == fixt.readText('push.txt').replace('WORKSPACE', workspace.canonicalPath)

            _ * pullRequest1.getRepository() >> repository1
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects', 'FactoryMethodName'])
    private void createWorkspaceFile(String repositoryFullName, String path, String content) {
        File file = new File(workspace, "$repositoryFullName/$path")
        file.parentFile.mkdirs()
        file.createNewFile()
        file.write(content)
    }

}
