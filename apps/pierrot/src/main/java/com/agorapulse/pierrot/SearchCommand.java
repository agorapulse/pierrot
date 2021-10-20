package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "search",
    description = "searches GitHub and prints the matching files"
)
public class SearchCommand implements Runnable {

    private static final String LINE = "-".repeat(80);
    private static final String DOUBLE_LINE = "=".repeat(80);

    @Parameters(
        arity = "1",
        description = "search term such as 'org:agorapulse filename:build.gradle'",
        paramLabel = "QUERY"
    )
    List<String> queries;

    @Option(
        names = {"-a", "--all"},
        description = "include archived repositories"
    )
    boolean all;

    @Option(
        names = {"-P", "--no-page"},
        description = "include archived repositories"
    )
    boolean noPage;


    @Inject GitHubService service;

    @Override
    public void run() {
        String query = String.join(" ", queries);
        AtomicInteger found = new AtomicInteger();

        System.out.println(DOUBLE_LINE);
        System.out.printf("Finding search results for '%s'!%n", query);

        if (!noPage) {
            System.out.println("Hit ENTER to continue to the next result or run with '--no-page' option to print everything at once");
        }

        service.search(query).forEach(content -> {
            if (!all && content.getRepository().isArchived()) {
                return;
            }

            found.incrementAndGet();

            System.out.println(DOUBLE_LINE);
            System.out.printf("| %s/%s%n", content.getRepository().getFullName(), content.getPath());
            System.out.println(DOUBLE_LINE);

            System.out.println(content.getTextContent());
            System.out.println(LINE);

            if (!noPage) {
                System.console().readLine();
            }
        });

        System.out.printf("Found %d results!%n", found.get());
    }
}
