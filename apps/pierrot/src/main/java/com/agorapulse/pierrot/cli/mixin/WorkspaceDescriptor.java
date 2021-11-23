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
package com.agorapulse.pierrot.cli.mixin;

import com.agorapulse.pierrot.api.source.PullRequestSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;

@Introspected
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class WorkspaceDescriptor implements PullRequestSource {

    private String project = "";

    private String branch = "";
    private String title = "";
    private String message = "";

    private String todoColumn = ProjectMixin.COLUMN_TO_DO;
    private String progressColumn = ProjectMixin.COLUMN_IN_PROGRESS;
    private String doneColumn = ProjectMixin.COLUMN_DONE;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String readBranch() {
        return branch;
    }

    @Override
    public String readTitle() {
        return title;
    }

    @Override
    public String readMessage() {
        return message;
    }

    public String getTodoColumn() {
        return todoColumn;
    }

    public void setTodoColumn(String todoColumn) {
        this.todoColumn = todoColumn;
    }

    public String getProgressColumn() {
        return progressColumn;
    }

    public void setProgressColumn(String progressColumn) {
        this.progressColumn = progressColumn;
    }

    public String getDoneColumn() {
        return doneColumn;
    }

    public void setDoneColumn(String doneColumn) {
        this.doneColumn = doneColumn;
    }
}
