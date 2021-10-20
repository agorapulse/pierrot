package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Repository;
import com.agorapulse.pierrot.mixin.PullRequestMixin;
import com.agorapulse.pierrot.mixin.SearchMixin;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.concurrent.atomic.AtomicInteger;

@Command(
    name = "replace",
    description = "searches GitHub and creates PRs to updated files"
)
public class ReplaceCommand implements Runnable {

    @Mixin SearchMixin search;
    @Mixin PullRequestMixin pullRequest;

    @Inject GitHubService service;

    @Option(
        names = {"-p", "--pattern"},
        description = "Java-style regular expression pattern to execute on the matched files",
        required = true
    )
    String pattern;

    @Option(
        names = {"-r", "--replacement"},
        description = "Java-style regular expression replacement",
        required = true
    )
    String replacement;

    @Override
    public void run() {
        String query = search.getQuery();
        AtomicInteger replaced = new AtomicInteger();

        System.out.printf("Finding search results to replace for '%s'!%n", query);

        service.search(query).forEach(content -> {
            if (!search.isAll() && content.getRepository().isArchived()) {
                return;
            }

            Repository ghr = service.getRepository(content.getRepository().getFullName()).get();

            if (ghr.isArchived()) {
                System.out.printf("Repository %s is archived. Nothing will be replaced.%n", ghr.getFullName());
                return;
            }

            if (!ghr.canWrite()) {
                System.out.printf("Current user does not have write rights to the repository %s. Nothing will be replaced.%n", ghr.getFullName());
                return;
            }

            System.out.printf("Replacing %s/%s%n", content.getRepository().getFullName(), content.getPath());

            ghr.createBranch(pullRequest.readBranch());

            if (content.replace(pullRequest.readBranch(), pullRequest.readMessage(), pattern, replacement)) {
                System.out.printf("Replaced %s/%s%n", content.getRepository().getFullName(), content.getPath());
                replaced.incrementAndGet();

                ghr.createPullRequest(pullRequest.readBranch(), pullRequest.readTitle(), pullRequest.readMessage()).ifPresent(url ->
                    System.out.printf("PR for %s available at %s%n", ghr.getFullName(), url)
                );
            }
        });

        System.out.printf("Replaced %d files%n", replaced.get());
    }

}
