package com.agorapulse.pierrot;

import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.ws.Workspace;
import com.agorapulse.pierrot.prompts.PullRequestPrompt;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        description = "the pull request message"
    )
    String message;

    @Option(
        names = {"-f", "--message-from"},
        description = "the file containing the pull request message"
    )
    File messageFrom;

    @Inject GitHubService service;

    @Inject Console console;

    @Override
    public void run() {
        PullRequestPrompt prompt = new PullRequestPrompt(console, branch, title, readMessage());

        prompt.ask();

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

            ghr.createBranch(prompt.getBranch());

            AtomicBoolean changed = new AtomicBoolean(false);
            r.visitFiles(f -> changed.set(ghr.writeFile(prompt.getBranch(), prompt.getMessage(), f.getPath(), f.getText()) || changed.get()));

            if (changed.get()) {
                ghr.createPullRequest(prompt.getBranch(), prompt.getTitle(), prompt.getMessage()).ifPresent(url ->
                    LOGGER.info("PR for {} available at {}", r.getName(), url)
                );
            }
        }));
    }

    private String readMessage() {
        if (StringUtils.isNotEmpty(message)) {
            return message;
        }

        if (messageFrom != null && messageFrom.exists()) {
            try {
                return Files.readString(messageFrom.toPath());
            } catch (IOException e) {
                LOGGER.error("Exception reading content of " + messageFrom, e);
            }
        }

        return null;
    }
}
