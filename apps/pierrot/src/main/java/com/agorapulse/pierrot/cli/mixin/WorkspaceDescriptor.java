package com.agorapulse.pierrot.cli.mixin;

import com.agorapulse.pierrot.api.source.PullRequestSource;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class WorkspaceDescriptor implements PullRequestSource {

    private String branch;
    private String title;
    private String project;
    private String message;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String readBranch() {
        return branch;
    }

    @Override
    public String readTitle() {
        return title;
    }

    @Override
    public String readMessage() {
        return message;
    }

}
