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
package com.agorapulse.pierrot.api.ws

import spock.lang.Specification
import spock.lang.TempDir

class WorkspaceSpec extends Specification {

    private static final String REPO_1 = 'agorapulse/foo'
    private static final String REPO_2 = 'agorapulse/bar'
    private static final String REPO_3 = 'musketyr/foo'
    private static final String PATH_1 = 'one'
    private static final String PATH_2 = 'somedir/nested/two'
    private static final String PATH_3 = 'somedir/nested/three'
    private static final String FILE_1 = "$REPO_1/$PATH_1"
    private static final String FILE_2 = "$REPO_1/$PATH_2"
    private static final String FILE_3 = "$REPO_3/$PATH_3"
    private static final String CONTENT_1 = 'foo'
    private static final String CONTENT_2 = 'bar'
    private static final String CONTENT_3 = 'baz'

    @TempDir File tsetWorkspace

    void 'visit repositories'() {
        given:
            new File(tsetWorkspace, REPO_1).mkdirs()
            new File(tsetWorkspace, REPO_2).mkdirs()
            new File(tsetWorkspace, REPO_3).mkdirs()

            writeFile FILE_1, CONTENT_1
            writeFile FILE_2, CONTENT_2
            writeFile FILE_3, CONTENT_3

        when:
            List<String> repoNames = []
            Map<String, String> fileContents = [:]
            Workspace workspace = new Workspace(tsetWorkspace)
            workspace.visitRepositories {
                repoNames.add(it.name)
                it.visitFiles {
                    fileContents.put it.path, it.text
                }
            }

        then:
            repoNames.size() == 3

            REPO_1 in repoNames
            REPO_2 in repoNames
            REPO_3 in repoNames

            fileContents.size() == 3

            PATH_1 in fileContents.keySet()
            PATH_2 in fileContents.keySet()
            PATH_3 in fileContents.keySet()

            fileContents[PATH_1] == CONTENT_1
            fileContents[PATH_2] == CONTENT_2
            fileContents[PATH_3] == CONTENT_3
    }

    private void writeFile(String path, String content) {
        File file1 = new File(tsetWorkspace, path)
        file1.parentFile.mkdirs()
        file1.write(content)
    }

}
