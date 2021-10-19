package com.agorapulse.pierrot.prompts;

import io.micronaut.core.util.StringUtils;

import java.io.Console;
import java.io.StringWriter;

public class PullRequestPrompt {

    private final Console console;

    private String branch;
    private String title;
    private String message;

    public PullRequestPrompt(Console console, String branch, String title, String message) {
        this.console = console;
        this.branch = branch;
        this.title = title;
        this.message = message;
    }

    public String getBranch() {
        return branch;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public void ask() {
        while (StringUtils.isEmpty(branch)) {
            this.branch = console.readLine("Branch Name: ");
        }

        while (StringUtils.isEmpty(title)) {
            this.title = console.readLine("Pull Request Title: ");
        }
        while (StringUtils.isEmpty(message)) {
            console.printf("Pull Request Message:%n");

            StringWriter writer = new StringWriter();

            int emptyLines = 0;

            while (emptyLines < 2) {
                String line = console.readLine();

                if (StringUtils.isEmpty(line)) {
                    emptyLines++;
                } else {
                    emptyLines = 0;
                }

                writer.append(line);
            }

            this.message = writer.toString();
        }
    }


}
