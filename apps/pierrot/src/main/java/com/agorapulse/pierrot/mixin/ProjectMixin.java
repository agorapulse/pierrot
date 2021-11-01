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
package com.agorapulse.pierrot.mixin;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Project;
import com.agorapulse.pierrot.core.PullRequest;
import io.micronaut.core.util.StringUtils;
import picocli.CommandLine;

import java.net.URI;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.UnaryOperator;

public class ProjectMixin {

    private final Scanner scanner = new Scanner(System.in);

    private UnaryOperator<String> reader = s -> {
        System.out.print(s);
        return scanner.nextLine();
    };

    @CommandLine.Option(
        names = {"-o", "--org"},
        description = "The organization owner of the project board"
    )
    String organization;

    @CommandLine.Option(
        names = {"--project"},
        description = "The name of the organization project"
    )
    String project;

    @CommandLine.Option(
        names = {"--column"},
        description = "The name of the column in the project",
        defaultValue = "To do"
    )
    String column;

    private Project board;

    public ProjectMixin() { }

    public ProjectMixin(String organization, String project, String column) {
        this.project = project;
        this.column = column;
        this.organization = organization;
    }

    public ProjectMixin(UnaryOperator<String> reader) {
        this.reader = reader;
    }

    public Optional<URI> addToProject(GitHubService service, String defaultOrganization, Optional<PullRequest> pullRequest) {
        if (StringUtils.isEmpty(project)) {
            // project must be specified to add projects
            return pullRequest.map(pr -> SearchMixin.toSafeUri(pr.getHtmlUrl()));
        }
        return pullRequest.map(pr -> {
            String currentColumn = readColumn(defaultOrganization);
            service.findOrCreateProject(readOrganization(defaultOrganization), readProject(defaultOrganization), currentColumn).ifPresent(p -> {
                p.addToColumn(currentColumn, pr);
                board = p;

            });
            return SearchMixin.toSafeUri(pr.getHtmlUrl());
        });
    }



    private String readOrganization(String defaultOrganization) {
        if (StringUtils.isEmpty(organization) && StringUtils.isNotEmpty(defaultOrganization)) {
            organization = defaultOrganization;
        }
        while (StringUtils.isEmpty(organization)) {
            organization = reader.apply("Organization: ");
        }

        return organization;
    }

    private String readProject(String defaultOrganization) {
        // read the organization first
        readOrganization(defaultOrganization);

        // the project should be always present but let's keep the loop in case of anything changes in the future
        while (StringUtils.isEmpty(project)) {
            this.project = reader.apply("Project (board): ");
        }
        return project;
    }

    private String readColumn(String defaultOrganization) {
        // read the project first
        readProject(defaultOrganization);
        while (StringUtils.isEmpty(column)) {
            this.column = reader.apply("Column: ");
        }
        return column;
    }

}
