package com.agorapulse.pierrot.api.event;

import com.agorapulse.pierrot.api.Project;

public class ProjectCreatedEvent {

    private final Project project;

    public ProjectCreatedEvent(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

}
