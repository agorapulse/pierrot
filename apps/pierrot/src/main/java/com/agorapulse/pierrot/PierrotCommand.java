package com.agorapulse.pierrot;

import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine.Command;

@Command(
    name = "pierrot",
    description = "the GitHub cross-repository governance tool",
    mixinStandardHelpOptions = true,
    subcommands = {
        DeleteCommand.class,
        PullCommand.class,
        PushCommand.class,
        ReplaceCommand.class,
        SearchCommand.class,
    }
)
public class PierrotCommand implements Runnable {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] { "--help" };
        }

        PicocliRunner.run(PierrotCommand.class, args);
    }

    public void run() {
        System.out.println("Use 'pierrot --help' for help");
    }
}
