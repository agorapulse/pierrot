package com.agorapulse.pierrot.core;

import java.net.URL;
import java.util.Optional;

public interface Repository {

    String getFullName();
    boolean isArchived();
    boolean createBranch(String name);
    Optional<URL> createPullRequest(String branch, String title, String message);
    boolean writeFile(String branch, String message, String path, String text);
}
