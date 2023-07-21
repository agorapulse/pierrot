package com.agorapulse.pierrot.api.event;

import com.agorapulse.pierrot.api.PullRequest;

public class PullRequestCreatedEvent {

    private final PullRequest pullRequest;

    public PullRequestCreatedEvent(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

}
