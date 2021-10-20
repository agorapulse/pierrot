package com.agorapulse.pierrot.mixin;

import io.micronaut.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class PullRequestMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestMixin.class);

    private UnaryOperator<String> reader = System.console()::readLine;
    private Consumer<String> writer = System.console()::printf;

    @CommandLine.Option(
        names = {"-b", "--branch"},
        description = "the pull request branch"
    )
    String branch;

    @CommandLine.Option(
        names = {"-t", "--title"},
        description = "the pull request title"
    )
    String title;

    @CommandLine.Option(
        names = {"-m", "--message"},
        description = "the pull request message"
    )
    String message;

    @CommandLine.Option(
        names = {"-f", "--message-from"},
        description = "the file containing the pull request message"
    )
    File messageFrom;

    public PullRequestMixin() { }

    public PullRequestMixin(String branch, String title, String message) {
        this.branch = branch;
        this.title = title;
        this.message = message;
    }

    public PullRequestMixin(UnaryOperator<String> reader, Consumer<String> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public String readBranch() {
        while (StringUtils.isEmpty(branch)) {
            this.branch = reader.apply("Branch Name: ");
        }

        return branch;
    }

    public String readTitle() {
        while (StringUtils.isEmpty(title)) {
            this.title = reader.apply("Pull Request Title: ");
        }
        return title;
    }

    public String readMessage() {
        // it's more convenient for the user to always ask for the title first, before the message
        readTitle();

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

        while (StringUtils.isEmpty(message)) {
            this.writer.accept("Pull Request Message (Markdown format, use triple new line to submit):%n");

            StringWriter stringWriter = new StringWriter();

            int emptyLines = 0;

            while (emptyLines < 2) {
                String line = reader.apply("> ");

                if (StringUtils.isEmpty(line)) {
                    emptyLines++;
                } else {
                    emptyLines = 0;
                }

                stringWriter.append(line);
            }

            this.message = stringWriter.toString();
        }

        return this.message;
    }

}
