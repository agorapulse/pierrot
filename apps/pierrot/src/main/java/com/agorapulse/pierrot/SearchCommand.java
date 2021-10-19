package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "search",
    description = "searches GitHub and prints the matching files"
)
public class SearchCommand implements Runnable {

    @Parameters(
        arity = "1",
        description = "search term such as 'org:agorapulse filename:build.gradle'",
        paramLabel = "QUERY"
    )
    List<String> queries;

    @Inject GitHubService service;

    @Override
    public void run() {
        String query = String.join(" ", queries);
        AtomicInteger found = new AtomicInteger();
        String doubleLine = "=".repeat(80);
        String line = "-".repeat(80);

        System.out.println(doubleLine);
        System.out.printf("Finding search results for '%s'!%n", query);
        service.search(query).forEach(content -> {
            found.incrementAndGet();

            System.out.println(doubleLine);
            System.out.printf("| %s/%s%n", content.getRepository().getFullName(), content.getPath());
            System.out.println(line);
            System.out.println(content.getTextContent());
            System.out.println(doubleLine);
        });

        System.out.printf("Found %d results!%n", found.get());
    }
}
