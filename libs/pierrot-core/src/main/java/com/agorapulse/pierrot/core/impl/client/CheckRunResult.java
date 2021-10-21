package com.agorapulse.pierrot.core.impl.client;

import com.agorapulse.pierrot.core.CheckRun;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class CheckRunResult implements CheckRun {

    private String name;
    private String status;
    private String conclusion;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

}
