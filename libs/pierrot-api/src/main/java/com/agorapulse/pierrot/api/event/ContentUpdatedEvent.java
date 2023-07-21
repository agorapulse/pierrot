package com.agorapulse.pierrot.api.event;

import com.agorapulse.pierrot.api.Content;

public class ContentUpdatedEvent {

    private final Content content;
    private final UpdateType updateType;

    public ContentUpdatedEvent(Content content, UpdateType updateType) {
        this.content = content;
        this.updateType = updateType;
    }

    public Content getContent() {
        return content;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

}
