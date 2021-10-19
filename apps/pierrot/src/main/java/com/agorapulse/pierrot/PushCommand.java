package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.ws.Workspace;
import com.agorapulse.pierrot.prompts.PullRequestPrompt;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.Console;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(
    name = "push",
    description = "searches GitHub and pulls the matching files locally"
)
public class PushCommand implements Runnable {

    @Option(
        names = {"-w", "--workspace"},
        description = "the working directory to pull found files",
        defaultValue = "."
    )
    File workspace;

    @Option(
        names = {"-b", "--branch"},
        description = "the pull request branch"
    )
    String branch;

    @Option(
        names = {"-t", "--title"},
        description = "the pull request title"
    )
    String title;

    @Option(
        names = {"-m", "--message"},
        description = "the pull request title"
    )
    String message;

    @Inject GitHubService service;

    @Inject Console console;

    @Override
    public void run() {
        PullRequestPrompt prompt = new PullRequestPrompt(console, branch, title, message);

        prompt.ask();

        Workspace ws = new Workspace(this.workspace);
        ws.visitRepositories(r -> service.getRepository(r.getName()).ifPresent(ghr -> {
            ghr.createBranch(prompt.getBranch());

            AtomicBoolean changed = new AtomicBoolean(false);
            r.visitFiles(f -> changed.set(ghr.writeFile(prompt.getBranch(), prompt.getMessage(), f.getPath(), f.getText()) || changed.get()));

            if (changed.get()) {
                ghr.createPullRequest(prompt.getBranch(), prompt.getTitle(), prompt.getMessage()).ifPresent(url ->
                    console.printf("PR for %s created at %s", r.getName(), url)
                );
            }
        }));


    }
}
