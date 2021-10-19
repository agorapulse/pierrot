package com.agorapulse.pierrot.core;

public interface ProgressReporter {

    void reportProgress(String message);
    void reportError(String message, Throwable error);

}
