package com.agorapulse.pierrot.api.event;

import com.agorapulse.pierrot.api.Content;
import com.agorapulse.pierrot.api.Repository;

public class ContentUpdatedEvent {

    private final Content content;
    private final UpdateType changeType;

    public ContentUpdatedEvent(Content content, UpdateType changeType) {
        this.content = content;
        this.changeType = changeType;
    }

    public Content getContent() {
        return content;
    }

    public UpdateType getChangeType() {
        return changeType;
    }

}
