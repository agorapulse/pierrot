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

import io.micronaut.configuration.picocli.PicocliRunner

class DeleteCommandSpec extends AbstractCommandSpec {

    String command = 'delete'

    void 'run command'() {
        when:
            String out = ConsoleOutput.capture {
                String[] args = [
                    'delete',
                    '-b',
                    BRANCH,
                    '-t',
                    TITLE,
                    '-m',
                    MESSAGE,
                    '--project',
                    PROJECT,
                    '-P',
                    CONTENT_SEARCH_TERM,
                ] as String[]
                PicocliRunner.run(PierrotCommand, context, args)
            }.out

        then:
            fixt.writeText('run.txt', out)
            out == fixt.readText('run.txt')
    }

}
