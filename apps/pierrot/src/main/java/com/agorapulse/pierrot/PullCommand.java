package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "pull",
    description = "searches GitHub and pulls the matching files locally"
)
public class PullCommand implements Runnable {

    private static final String DOUBLE_LINE = "=".repeat(80);

    @Parameters(
        arity = "1",
        description = "search term such as 'org:agorapulse filename:build.gradle'",
        paramLabel = "QUERY"
    )
    List<String> queries;

    @Option(
        names = {"-w", "--workspace"},
        description = "the working directory to pull found files",
        defaultValue = "."
    )
    File workspace;

    @Option(
        names = {"-a", "--all"},
        description = "include archived repositories"
    )
    boolean all;

    @Inject GitHubService service;

    @Override
    public void run() {
        String query = String.join(" ", queries);
        AtomicInteger found = new AtomicInteger();

        System.out.println(DOUBLE_LINE);
        System.out.printf("Finding search results for '%s'!%n", query);
        service.search(query).forEach(content -> {
            if (!all && content.getRepository().isArchived()) {
                return;
            }

            found.incrementAndGet();

            File location = new File(workspace, String.format("%s/%s", content.getRepository().getFullName(), content.getPath()));
            content.writeTo(location);
            System.out.printf("Fetched %s/%s%n", content.getRepository().getFullName(), content.getPath());
        });

        System.out.printf("Found %d results!%n", found.get());
    }
}
