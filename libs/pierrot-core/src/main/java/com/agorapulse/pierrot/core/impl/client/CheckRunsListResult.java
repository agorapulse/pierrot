package com.agorapulse.pierrot.core.impl.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class CheckRunsListResult {

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("check_runs")
    private List<CheckRunResult> checkRuns;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<CheckRunResult> getCheckRuns() {
        return checkRuns;
    }

    public void setCheckRuns(List<CheckRunResult> checkRuns) {
        this.checkRuns = checkRuns;
    }

}
