package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.ws.Workspace;
import com.agorapulse.pierrot.mixin.PullRequestMixin;
import com.agorapulse.pierrot.mixin.WorkspaceMixin;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.concurrent.atomic.AtomicBoolean;

@Command(
    name = "push",
    description = "searches GitHub and pulls the matching files locally"
)
public class PushCommand implements Runnable {

    @Mixin WorkspaceMixin workspace;
    @Mixin PullRequestMixin pullRequest;

    @Inject GitHubService service;

    @Override
    public void run() {
        Workspace ws = new Workspace(workspace.getWorkspace());
        ws.visitRepositories(r -> service.getRepository(r.getName()).ifPresent(ghr -> {
            if (ghr.isArchived()) {
                System.out.printf("Repository %s is archived. Nothing will be pushed.%n", r.getName());
                return;
            }

            if (!ghr.canWrite()) {
                System.out.printf("Current user does not have write rights to the repository %s. Nothing will be pushed.%n", r.getName());
                return;
            }

            ghr.createBranch(pullRequest.readBranch());

            AtomicBoolean changed = new AtomicBoolean(false);
            r.visitFiles(f -> changed.set(ghr.writeFile(pullRequest.readBranch(), pullRequest.readMessage(), f.getPath(), f.getText()) || changed.get()));

            if (changed.get()) {
                ghr.createPullRequest(pullRequest.readBranch(), pullRequest.readTitle(), pullRequest.readMessage()).ifPresent(url ->
                    System.out.printf("PR for %s available at %s%n", r.getName(), url)
                );
            }
        }));
    }

}
