package com.agorapulse.pierrot.mixin;

import picocli.CommandLine.Option;

import java.io.File;

public class WorkspaceMixin {

    @Option(
        names = {"-w", "--workspace"},
        description = "the working directory to pull found files",
        defaultValue = "."
    )
    File workspace;

    public WorkspaceMixin() { }

    public WorkspaceMixin(File workspace) {
        this.workspace = workspace;
    }

    public File getWorkspace() {
        return workspace;
    }
}

