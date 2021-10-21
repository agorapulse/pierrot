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

import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine.Command;

@Command(
    name = "pierrot",
    description = "The GitHub cross-repository governance tool",
    mixinStandardHelpOptions = true,
    subcommands = {
        DeleteCommand.class,
        PullCommand.class,
        PushCommand.class,
        ReplaceCommand.class,
        SearchCommand.class,
        StatusCommand.class,
    }
)
public class PierrotCommand implements Runnable {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] {"--help"};
        }

        if (System.getenv("GITHUB_TOKEN") == null) {
            System.out.println("Please, set up your GitHub token as GITHUB_TOKEN environment variable");
        }

        PicocliRunner.run(PierrotCommand.class, args);
    }

    public void run() {
        System.out.println("Use 'pierrot --help' for help");
    }
}
