/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2022 Vladimir Orany.
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

import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import com.agorapulse.pierrot.api.summary.SummaryCollector;
import com.agorapulse.pierrot.cli.summary.SummaryWriter;
import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.CommandLinePropertySource;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.MapPropertySource;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.micronaut.core.annotation.TypeHint.AccessType.*;

@Command(
    name = "pierrot",
    description = "The GitHub cross-repository governance tool",
    mixinStandardHelpOptions = true,
    subcommands = {
        CreateCommand.class,
        DeleteCommand.class,
        InitCommand.class,
        PullCommand.class,
        PushCommand.class,
        ReplaceCommand.class,
        SearchCommand.class,
        StatusCommand.class,
    }
)
@TypeHint(
    typeNames = {
        "com.agorapulse.pierrot.cli.mixin.WorkspaceDescriptor"
    },
    accessType = {
        ALL_PUBLIC,
        ALL_DECLARED_CONSTRUCTORS,
        ALL_PUBLIC_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
        ALL_PUBLIC_METHODS,
        ALL_PUBLIC_FIELDS
    }
)
public class PierrotCommand implements Runnable {

    private static final String GITHUB_TOKEN_NAME = "token";
    private static final String GITHUB_TOKEN_PARAMETER = "--" + GITHUB_TOKEN_NAME;

    private static final String SUMMARY_TOKEN_NAME = "summary-file";

    private static final String SUMMARY_TOKEN_PARAMETER = "--" + SUMMARY_TOKEN_NAME;

    @CommandLine.Option(
        names = {"-s", "--stacktrace"},
        description = "Print stack traces",
        scope = CommandLine.ScopeType.INHERIT
    )
    void setStacktrace(boolean stacktrace) {
        if (stacktrace) {
            LoggerWithOptionalStacktrace.enableStacktrace();
        }
    }

    @CommandLine.Option(
        names = {GITHUB_TOKEN_PARAMETER},
        description = "The GitHub token",
        scope = CommandLine.ScopeType.INHERIT
    )
    String token;

    @CommandLine.Option(
        names = {SUMMARY_TOKEN_PARAMETER},
        description = "Markdown summary file path",
        scope = CommandLine.ScopeType.INHERIT
    )
    File summaryFile;

    @Inject @Nullable
    SummaryCollector summaryCollector;

    public static void main(String[] args) {
        System.exit(execute(args));
    }

    static int execute(String[] args) {
        if (args.length == 0) {
            args = new String[]{"--help"};
        }

        io.micronaut.core.cli.CommandLine commandLine = io.micronaut.core.cli.CommandLine.parse(args);

        CommandLinePropertySource commandLinePropertySource = new CommandLinePropertySource(commandLine);

        Map<String, Object> map = new HashMap<>();

        if (commandLine.getUndeclaredOptions().containsKey(GITHUB_TOKEN_NAME)) {
            map.put("pierrot.token", commandLine.getUndeclaredOptions().get(GITHUB_TOKEN_NAME));
        }

        if (commandLine.getUndeclaredOptions().containsKey(SUMMARY_TOKEN_NAME)) {
            try {
                map.put("summary.file", ((File) commandLine.getUndeclaredOptions().get(SUMMARY_TOKEN_NAME)).getCanonicalPath());
            } catch (IOException e) {
                System.out.println("Cannot get canonical path for " + commandLine.getUndeclaredOptions().get(SUMMARY_TOKEN_NAME) + ": " + e.getMessage());
            }
        } else if (System.getenv("GITHUB_STEP_SUMMARY") != null) {
            map.put("summary.file", System.getenv("GITHUB_STEP_SUMMARY"));
        }

        MapPropertySource mapSource = MapPropertySource.of("command-line-properties", map);

        ApplicationContextBuilder builder = ApplicationContext
            .builder(PierrotCommand.class, Environment.CLI)
            .propertySources(commandLinePropertySource, mapSource);

        int exitCode;

        try (ApplicationContext ctx = builder.start()) {
            CommandLine cmd = new CommandLine(PierrotCommand.class, new MicronautFactory(ctx));
            exitCode = cmd.execute(args);
            if (ctx.containsBean(SummaryWriter.class) && !Arrays.asList(args).contains("--help")) {
                File summaryFileLocation = ctx.getBean(SummaryWriter.class).write();
                System.out.println("Summary file written to " + summaryFileLocation);
            }
        }


        return exitCode;
    }

    public void run() {
        System.out.println("Use 'pierrot --help' for help");
    }
}
