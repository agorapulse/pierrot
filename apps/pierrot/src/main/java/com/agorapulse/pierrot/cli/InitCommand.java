/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Vladimir Orany.
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
package com.agorapulse.pierrot.cli;

import com.agorapulse.pierrot.cli.mixin.ProjectMixin;
import com.agorapulse.pierrot.cli.mixin.PullRequestMixin;
import com.agorapulse.pierrot.cli.mixin.WorkspaceMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "init",
    description = "initializes new workspace",
    mixinStandardHelpOptions = true
)
public class InitCommand implements Runnable {

    @Mixin WorkspaceMixin workspace;
    @Mixin PullRequestMixin pullRequest;
    @Mixin ProjectMixin project;


    @Override
    public void run() {
        System.out.println("Initializing Pierrot workspace in " + workspace.getWorkspace().getAbsolutePath());

        workspace.initWorkspaceFiles(pullRequest, project);
    }

}
