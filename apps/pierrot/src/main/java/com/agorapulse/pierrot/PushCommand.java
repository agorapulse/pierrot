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
package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.ws.Workspace;
import com.agorapulse.pierrot.mixin.PullRequestMixin;
import com.agorapulse.pierrot.mixin.WorkspaceMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.concurrent.atomic.AtomicBoolean;

@Command(
    name = "push",
    description = "pushes the the local changes and creates PRs"
)
public class PushCommand implements Runnable {

    @Mixin WorkspaceMixin workspace;
    @Mixin PullRequestMixin pullRequest;

    @Inject GitHubService service;

    @Override
    public void run() {
        System.out.printf("Pushing changes from %s%n", workspace.getWorkspace());
        Workspace ws = new Workspace(workspace.getWorkspace());
        ws.visitRepositories(r ->
            pullRequest.createPullRequest(service, r.getName(), (ghr, branch, message) -> {
            AtomicBoolean changed = new AtomicBoolean(false);
            r.visitFiles(f -> changed.set(ghr.writeFile(branch, message, f.getPath(), f.getText()) || changed.get()));
            return changed.get();
        }));
        System.out.printf("Opened %d pull requests %n", pullRequest.getPullRequestsCreated());
    }

}
