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
package com.agorapulse.pierrot.api.event;

import com.agorapulse.pierrot.api.Project;
import com.agorapulse.pierrot.api.PullRequest;

public class PullRequestAddedToProjectEvent {

    private final Project project;
    private final PullRequest pullRequest;

    public PullRequestAddedToProjectEvent(Project project, PullRequest pullRequest) {
        this.project = project;
        this.pullRequest = pullRequest;
    }

    public Project getProject() {
        return project;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }
}
