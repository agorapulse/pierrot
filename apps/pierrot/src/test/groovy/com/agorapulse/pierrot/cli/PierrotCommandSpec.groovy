/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Vladimir Orany.
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
package com.agorapulse.pierrot.cli

class PierrotCommandSpec extends AbstractCommandSpec {

    List<String> args = [
        '--help',
        '--token=xazabc',
    ]

    List<String> helpArgs = [
        '--help',
    ]

    void 'run main'() {
        given:
            List<String> testArgs = ['--help', '--token=token', '-s']
        when:
            TestConsole console = TestConsole.capture {
                PierrotCommand.execute(testArgs as String[])
            }

        then:
            compareOutput('help.txt', console, testArgs)
    }

    void 'run main without token'() {
        when:
            TestConsole console = TestConsole.capture {
                PierrotCommand.execute(helpArgs as String[])
            }

        then:
            compareOutput('help-no-token.txt', console, helpArgs)
    }

}
