package com.agorapulse.pierrot.core;

import java.util.stream.Stream;

public interface PullRequest {

    Repository getRepository();

    String getTitle();

    String getBody();

    boolean isMerged();

    boolean isMergeable();

    Stream<? extends CheckRun> getChecks();

}
