package com.agorapulse.pierrot.api.event;

import com.agorapulse.pierrot.api.Project;
import com.agorapulse.pierrot.api.PullRequest;

public class PullRequestAddedToProjectEvent {

    private final Project project;
    private final PullRequest pullRequest;

    public PullRequestAddedToProjectEvent(Project project, PullRequest pullRequest) {
        this.project = project;
        this.pullRequest = pullRequest;
    }

    public Project getProject() {
        return project;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }
}
