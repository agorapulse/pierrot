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
import io.micronaut.configuration.picocli.PicocliRunner

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class PullCommandSpec extends AbstractCommandSpec {

    GitHubService service = Mock {
        searchContent(CONTENT_SEARCH_TERM, false) >> {
            Stream.of(content1, content2, content3)
        }
    }

    String command = 'pull'

    void 'run command'() {
        when:
            ConsoleOutput console = ConsoleOutput.capture {
                String[] args = [
                    'pull',
                    '-P',
                    '-w',
                    workspace.canonicalPath,
                    CONTENT_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }

            File file1 = new File(workspace, "$REPOSITORY_ONE/$PATH")
            File file2 = new File(workspace, "$REPOSITORY_TWO/$PATH")
            File file3 = new File(workspace, "$REPOSITORY_TWO/prefix/$PATH")

        then:
            console.out == fixt.readText('run.txt')

            file1.exists()
            file1.text == CONTENT

            file2.exists()
            file2.text == CONTENT.reverse()

            file3.exists()
            file3.text == CONTENT
    }

}
