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
package com.agorapulse.pierrot.cli.mixin;

import com.agorapulse.pierrot.api.source.PullRequestSource;
import com.agorapulse.pierrot.api.source.WorkspaceSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class WorkspaceMixin implements WorkspaceSource {

    private static final String WORKPLACE_FILE_NAME = "pierrot.yml";

    @Option(
        names = {"-w", "--workspace"},
        description = "The workspace directory",
        defaultValue = "."
    )
    File workspace;

    @Override
    public File getWorkspace() {
        return workspace;
    }

    public Optional<PullRequestSource> asPullRequestSource() {
        File workplaceFile = new File(workspace, WORKPLACE_FILE_NAME);

        if (!workplaceFile.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(createYamlMapper().readValue(workplaceFile, WorkspaceDescriptor.class));
        } catch (IOException e) {
            System.err.println("Cannot read workspace file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<String> readProjectName() {
        File workplaceFile = new File(workspace, WORKPLACE_FILE_NAME);

        if (!workplaceFile.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(createYamlMapper().readValue(workplaceFile, WorkspaceDescriptor.class).getProject());
        } catch (IOException e) {
            System.err.println("Cannot read workspace file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void initWorkspaceFiles(PullRequestSource pullRequestSource, ProjectMixin projectSource) {
        File workplaceFile = new File(workspace, WORKPLACE_FILE_NAME);
        workplaceFile.getParentFile().mkdirs();

        WorkspaceDescriptor descriptor = new WorkspaceDescriptor();


        projectSource.storeInto(descriptor);

        descriptor.setBranch(pullRequestSource.readBranch());
        descriptor.setTitle(pullRequestSource.readTitle());
        descriptor.setMessage(pullRequestSource.readMessage());


        try {
            createYamlMapper().writeValue(workplaceFile, descriptor);
        } catch (IOException e) {
            System.err.println("Cannot write workspace file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private ObjectMapper createYamlMapper() {
        return new ObjectMapper(
            new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        ).setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}

