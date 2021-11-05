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
package com.agorapulse.pierrot.cli

class CreateCommandSpec extends AbstractCommandSpec {

    List<String> args = [
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
    ]

    void 'use file to upload content'() {
        given:
            File content = createWorkspaceFile(REPOSITORY_ONE, '.testfile', CONTENT)
            File message = createWorkspaceFile(REPOSITORY_ONE, 'prefix/message.txt', MESSAGE)
        expect:
            runCommand('file.txt', [
                'create',
                '-b',
                BRANCH,
                '-t',
                TITLE,
                '--message-from',
                message.canonicalPath,
                '--from',
                content.canonicalPath,
                '-P',
                CONTENT_SEARCH_TERM,
            ])
    }

}
