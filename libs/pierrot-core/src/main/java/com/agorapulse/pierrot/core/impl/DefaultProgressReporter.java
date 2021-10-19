package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.ProgressReporter;
import jakarta.inject.Singleton;

@Singleton
public class DefaultProgressReporter implements ProgressReporter {

    @Override
    public void reportProgress(String message) {
        System.out.println(message);
    }

    @Override
    public void reportError(String message, Throwable error) {
        System.err.println(message);
    }

}
