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
import io.micronaut.configuration.picocli.PicocliRunner

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class ReplaceCommandSpec extends AbstractCommandSpec {

    private static final String REPLACEMENT = 'salut $1'
    private static final String PATTERN = /hello (\w+)/

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

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)

        searchContent(CONTENT_SEARCH_TERM, false) >> {
            Stream.of(content1, content2, content3)
        }

        findOrCreateProject(OWNER, PROJECT, _ as String) >> Optional.of(project)
    }

    String command = 'replace'

    void 'run command'() {
        when:
            ConsoleOutput console = ConsoleOutput.capture {
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
                    CONTENT_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }

        then:
            !console.err
            console.out == fixt.readText('run.txt')
    }

}
