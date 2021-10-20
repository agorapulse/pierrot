package com.agorapulse.pierrot;

import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "pierrot",
    description = "the GitHub multirepository governance tool",
    mixinStandardHelpOptions = true,
    subcommands = {
        PullCommand.class,
        PushCommand.class,
        SearchCommand.class
    }
)
public class PierrotCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(PierrotCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            System.out.println("Hi!");
        }
    }
}
