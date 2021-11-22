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

import com.agorapulse.pierrot.api.source.ProjectSource;
import com.agorapulse.pierrot.api.source.PullRequestSource;
import com.agorapulse.pierrot.api.source.WorkspaceSource;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class WorkspaceMixin implements WorkspaceSource {

    private static final String WORKPLACE_FILE_NAME = "pierrot.yml";
    private static final String COMMIT_MESSAGE_FILE = "COMMIT_MESSAGE.md";

    @Option(
        names = {"-w", "--workspace"},
        description = "The working directory to pull found files",
        defaultValue = "."
    )
    File workspace;

    @Override
    public File getWorkspace() {
        return workspace;
    }

    @SuppressWarnings("unchecked")
    public Optional<PullRequestSource> asPullRequestSource() {
        File workplaceFile = new File(workspace, WORKPLACE_FILE_NAME);

        if (!workplaceFile.exists()) {
            return Optional.empty();
        }

        try {
            Yaml yaml = new Yaml();
            Object content = yaml.load(new FileReader(workplaceFile));
            if (content instanceof Map) {
                Map<String, Object> values = (Map<String, Object>) content;
                return Optional.of(new PullRequestSource() {
                    @Override
                    public String readBranch() {
                        return values.containsKey("branch") ? String.valueOf(values.get("branch")) : null;
                    }

                    @Override
                    public String readTitle() {
                        return values.containsKey("title") ? String.valueOf(values.get("title")) : null;
                    }

                    @Override
                    public String readMessage() {
                        File commitMessageFile = new File(workspace, COMMIT_MESSAGE_FILE);
                        if (commitMessageFile.exists()) {
                            try {
                                return Files.readString(commitMessageFile.toPath());
                            } catch (IOException e) {
                                System.err.println("Cannot read commit message file " + workplaceFile.getAbsolutePath());
                                e.printStackTrace();
                            }
                        }
                        return values.containsKey("message") ? String.valueOf(values.get("message")) : null;
                    }
                });
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot read workspace file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public Optional<String> readProjectName() {
        File workplaceFile = new File(workspace, WORKPLACE_FILE_NAME);

        if (!workplaceFile.exists()) {
            return Optional.empty();
        }

        try {
            Yaml yaml = new Yaml();
            Object content = yaml.load(new FileReader(workplaceFile));
            if (content instanceof Map) {
                Map<String, Object> values = (Map<String, Object>) content;
                return values.containsKey("project") ? Optional.of(String.valueOf(values.get("project"))) : Optional.empty();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot read workspace file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void initWorkspaceFiles(PullRequestSource pullRequestSource, ProjectSource projectSource) {
        File workplaceFile = new File(workspace, WORKPLACE_FILE_NAME);
        workplaceFile.mkdirs();

        Map<String, String> storedValues = new LinkedHashMap<>();

        projectSource.getProject().ifPresent(p -> {
            storedValues.put("project", p.getName());
        });


        storedValues.putAll(Map.of(
            "branch", pullRequestSource.readBranch(),
            "title", pullRequestSource.readTitle()
        ));


        Yaml yaml = new Yaml();

        try {
            yaml.dump(storedValues, new FileWriter(workplaceFile));
        } catch (IOException e) {
            System.err.println("Cannot write workspace file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
        }

        File commitMessageFile = new File(workspace, COMMIT_MESSAGE_FILE);
        commitMessageFile.mkdirs();

        try {
            Files.write(commitMessageFile.toPath(), pullRequestSource.readMessage().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Cannot write commit message file " + workplaceFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

}

