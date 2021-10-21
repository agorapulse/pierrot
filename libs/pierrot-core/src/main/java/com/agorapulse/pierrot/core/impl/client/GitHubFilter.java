package com.agorapulse.pierrot.core.impl.client;

import com.agorapulse.pierrot.core.GitHubConfiguration;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

@GitHub
@Singleton
public class GitHubFilter implements HttpClientFilter {

    private final GitHubConfiguration configuration;

    public GitHubFilter(GitHubConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        return chain.proceed(
            request
                .header("User-Agent", "Pierrot")
                .header("Authorization", "token " + configuration.getToken())
        );
    }

}
