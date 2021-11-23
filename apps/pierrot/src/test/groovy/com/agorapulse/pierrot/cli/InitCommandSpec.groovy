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

import com.agorapulse.pierrot.cli.mixin.WorkspaceDescriptor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

class InitCommandSpec extends AbstractCommandSpec {

    List<String> getArgs() {
        return [
            'init',
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

    @Override
    protected String expandFile(String input) {
        return input.replace('WORKSPACE', workspace.canonicalPath)
    }

    @Override
    protected boolean additionalChecks() {
        File workspaceFile = new File(workspace, 'pierrot.yml')

        assert workspaceFile.exists()

        WorkspaceDescriptor descriptor = new ObjectMapper(new YAMLFactory()).readValue(workspaceFile, WorkspaceDescriptor)

        assert descriptor.branch == BRANCH
        assert descriptor.title == TITLE
        assert descriptor.message == MESSAGE
        assert descriptor.project == PROJECT

        return true
    }

}
