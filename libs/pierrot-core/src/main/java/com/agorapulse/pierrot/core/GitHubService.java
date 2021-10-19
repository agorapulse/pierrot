package com.agorapulse.pierrot.core;

import java.util.Optional;
import java.util.stream.Stream;

public interface GitHubService {
    Stream<Content> search(String query);

    Optional<Repository> getRepository(String repositoryFullName);
}
