package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.ws.Workspace;
import com.agorapulse.pierrot.mixin.PullRequestMixin;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(
    name = "push",
    description = "searches GitHub and pulls the matching files locally"
)
public class PushCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushCommand.class);

    @Option(
        names = {"-w", "--workspace"},
        description = "the working directory to pull found files",
        defaultValue = "."
    )
    File workspace;

    @Mixin PullRequestMixin pullRequest;

    @Inject GitHubService service;

    @Override
    public void run() {
        Workspace ws = new Workspace(this.workspace);
        ws.visitRepositories(r -> service.getRepository(r.getName()).ifPresent(ghr -> {
            if (ghr.isArchived()) {
                LOGGER.info("Repository {} is archived. Nothing will be pushed.", r.getName());
                return;
            }

            if (!ghr.canWrite()) {
                LOGGER.info("Current user does not have write rights to the repository {}. Nothing will be pushed.", r.getName());
                return;
            }

            ghr.createBranch(pullRequest.readBranch());

            AtomicBoolean changed = new AtomicBoolean(false);
            r.visitFiles(f -> changed.set(ghr.writeFile(pullRequest.readBranch(), pullRequest.readMessage(), f.getPath(), f.getText()) || changed.get()));

            if (changed.get()) {
                ghr.createPullRequest(pullRequest.readBranch(), pullRequest.readTitle(), pullRequest.readMessage()).ifPresent(url ->
                    LOGGER.info("PR for {} available at {}", r.getName(), url)
                );
            }
        }));
    }

}
