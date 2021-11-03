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

import spock.lang.TempDir

class PushCommandSpec extends AbstractCommandSpec {

    @TempDir
    File workspace

    String command = 'push'

    List<String> getArgs() {
        return [
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
        ]
    }

    void setup() {
        createWorkspaceFile(REPOSITORY_ONE, PATH, CONTENT)
        createWorkspaceFile(REPOSITORY_TWO, PATH, CONTENT.reverse())
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects', 'FactoryMethodName'])
    private void createWorkspaceFile(String repositoryFullName, String path, String content) {
        File file = new File(workspace, "$repositoryFullName/$path")
        file.parentFile.mkdirs()
        file.createNewFile()
        file.write(content)
    }

    @Override
    protected String fixRunFile(String input) {
        return input.replace('WORKSPACE', workspace.canonicalPath)
    }

}
