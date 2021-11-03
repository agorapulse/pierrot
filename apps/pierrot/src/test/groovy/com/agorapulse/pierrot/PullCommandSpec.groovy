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
import com.agorapulse.pierrot.core.Repository
import io.micronaut.configuration.picocli.PicocliRunner
import spock.lang.TempDir

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class PullCommandSpec extends AbstractCommandSpec {

    @TempDir File workspace

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
    }

    Content content1 = Mock {
        getRepository() >> repository1
        getPath() >> PATH
        writeTo(_ as File) >> { File f ->
            f.parentFile.mkdirs()
            f.createNewFile()
            f.write(CONTENT)
        }
    }

    Content content2 = Mock {
        getRepository() >> repository2
        getPath() >> PATH
        writeTo(_ as File) >> { File f ->
            f.parentFile.mkdirs()
            f.createNewFile()
            f.write(CONTENT.reverse())
        }
    }

    GitHubService service = Mock {
        searchContent(CONTENT_SEARCH_TERM, false) >> {
            Stream.of(content1, content2)
        }
    }

    String command = 'pull'

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
                String[] args = [
                    'pull',
                    '-P',
                    '-w',
                    workspace.canonicalPath,
                    CONTENT_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

            File file1 = new File(workspace, "$REPOSITORY_ONE/$PATH")
            File file2 = new File(workspace, "$REPOSITORY_TWO/$PATH")
        then:
            out == fixt.readText('pull.txt')

            file1.exists()
            file1.text == CONTENT

            file2.exists()
            file2.text == CONTENT.reverse()
    }

}
