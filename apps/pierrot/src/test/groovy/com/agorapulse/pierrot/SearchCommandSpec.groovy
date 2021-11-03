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

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
class SearchCommandSpec extends AbstractCommandSpec {

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
    }

    Content content1 = Mock {
        getRepository() >> repository1
        getPath() >> PATH
        getHtmlUrl() >> "https://example.com/$REPOSITORY_ONE/$PATH"
        getTextContent() >> CONTENT
    }

    Content content2 = Mock {
        getRepository() >> repository2
        getHtmlUrl() >> "https://example.com/$REPOSITORY_TWO/$PATH"
        getPath() >> PATH
        getTextContent() >> CONTENT.reverse()
    }

    GitHubService service = Mock {
        searchContent(CONTENT_SEARCH_TERM, false) >> {
            Stream.of(content1, content2)
        }
    }

    String command = 'search'

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
                String[] args = [
                    'search',
                    '-P',
                    CONTENT_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

        then:
            out == fixt.readText('search.txt')
    }

}
