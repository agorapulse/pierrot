package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Project;
import com.agorapulse.pierrot.core.PullRequest;
import org.kohsuke.github.GHProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class DefaultProject implements Project {

    // the field is not static to prevent GraalVM FileAppender issues
    private final Logger logger = LoggerFactory.getLogger(DefaultProject.class);

    private final GHProject project;

    public DefaultProject(GHProject project) {
        this.project = project;
    }

    @Override
    public void addToColumn(String column, PullRequest pr) {
        try {
            StreamSupport.stream(project.listColumns().spliterator(), false)
                .filter(col -> column.equals(col.getName()))
                .findFirst()
                .or(() -> {
                    try {
                        return Optional.of(project.createColumn(column));
                    } catch (IOException e) {
                        logger.error("Exception creating column " + column + " in project " + project.getName(), e);
                        return Optional.empty();
                    }
                })
                .ifPresent(col -> {
                    if (pr instanceof DefaultPullRequest) {
                        try {
                            col.createCard(((DefaultPullRequest) pr).getNativePullRequest());
                        } catch (IOException e) {
                            logger.error("Exception adding PR to the column " + column + " in project " + project.getName(), e);
                        }
                    } else {
                        logger.error("Cannot add PR to the column " + column + " in project " + project.getName() + " - wrong type");
                    }
                });
        } catch (IOException e) {
            logger.error("Exception adding PR to the column " + column + " in project " + project.getName(), e);
        }
    }
}
