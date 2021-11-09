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

class PullCommandSpec extends AbstractCommandSpec {

    List<String> getArgs() {
        return [
            'pull',
            '-P',
            '-o',
            '-w',
            workspace.canonicalPath,
            CONTENT_SEARCH_TERM,
        ]
    }

    void setup() {
        File file = new File(workspace, "$REPOSITORY_ONE/.keepme")
        file.parentFile.mkdirs()
        file.createNewFile()
    }

    @Override
    protected boolean additionalChecks() {
        File file1 = new File(workspace, "$REPOSITORY_ONE/$PATH")
        File file2 = new File(workspace, "$REPOSITORY_TWO/$PATH")
        File file3 = new File(workspace, "$REPOSITORY_TWO/prefix/$PATH")

        assert file1.exists()
        assert file1.text == CONTENT
        assert file2.exists()
        assert file2.text == CONTENT.reverse()
        assert file3.exists()
        assert file3.text == CONTENT

        return true
    }

}
