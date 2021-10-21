package com.agorapulse.pierrot.core.impl.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.jackson.annotation.JacksonFeatures;

@GitHub
@Client("github")
@JacksonFeatures(
    disabledDeserializationFeatures = DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
)
public interface GitHubHttpClient {

    String GITHUB_V_3_JSON = "application/vnd.github.v3+json";

    @Get("/repos/{owner}/{repo}/commits/{sha}/check-runs")
    @Consumes(GITHUB_V_3_JSON)
    CheckRunsListResult getCheckRuns(String owner, String repo, String sha);

}
