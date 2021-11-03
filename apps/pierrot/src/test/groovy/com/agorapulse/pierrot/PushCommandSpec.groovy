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
import com.agorapulse.pierrot.core.Repository
import io.micronaut.configuration.picocli.PicocliRunner
import spock.lang.TempDir

@SuppressWarnings('UnnecessaryGetter')
class PushCommandSpec extends AbstractCommandSpec {

    @TempDir File workspace

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

    String command = 'push'

    void setup() {
        createWorkspaceFile(REPOSITORY_ONE, PATH, CONTENT)
        createWorkspaceFile(REPOSITORY_TWO, PATH, CONTENT.reverse())
    }

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
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
            }.out

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
