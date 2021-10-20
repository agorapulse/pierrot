package com.agorapulse.pierrot.mixin;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

public class SearchMixin {

    @Parameters(
        arity = "1",
        description = "search term such as 'org:agorapulse filename:build.gradle'",
        paramLabel = "QUERY"
    )
    List<String> queries;

    @Option(
        names = {"-a", "--all"},
        description = "include archived repositories"
    )
    boolean all;

    public SearchMixin() { }

    public SearchMixin(List<String> queries, boolean all) {
        this.queries = queries;
        this.all = all;
    }

    public List<String> getQueries() {
        return queries;
    }

    public String getQuery() {
        return String.join(" ", queries);
    }

    public boolean isAll() {
        return all;
    }

}
