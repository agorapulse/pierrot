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
package com.agorapulse.pierrot.cli;

import com.agorapulse.pierrot.core.util.LoggerWithOptionalStacktrace;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.CommandLinePropertySource;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.MapPropertySource;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(
    name = "pierrot",
    description = "The GitHub cross-repository governance tool",
    mixinStandardHelpOptions = true,
    subcommands = {
        CreateCommand.class,
        DeleteCommand.class,
        PullCommand.class,
        PushCommand.class,
        ReplaceCommand.class,
        SearchCommand.class,
        StatusCommand.class,
    }
)
public class PierrotCommand implements Runnable {

    private static final String GITHUB_TOKEN_NAME = "github-token";
    private static final String GITHUB_TOKEN_PARAMETER = "--" + GITHUB_TOKEN_NAME;

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

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] {"--help"};
        }

        io.micronaut.core.cli.CommandLine commandLine = io.micronaut.core.cli.CommandLine.parse(args);

        CommandLinePropertySource commandLinePropertySource = new CommandLinePropertySource(commandLine);

        Map<String, Object> map = commandLine.getUndeclaredOptions().containsKey(GITHUB_TOKEN_NAME)
            ? Map.of("github.token", commandLine.getUndeclaredOptions().get(GITHUB_TOKEN_NAME))
            : Map.of();


        MapPropertySource mapSource = MapPropertySource.of("token-from-command-line", map);

        ApplicationContextBuilder builder = ApplicationContext
            .builder(PierrotCommand.class, Environment.CLI)
            .propertySources(commandLinePropertySource, mapSource);

        try (ApplicationContext ctx = builder.start()) {
            PicocliRunner.run(PierrotCommand.class, ctx, args);
        }
    }

    public void run() {
        System.out.println("Use 'pierrot --help' for help");
    }
}
