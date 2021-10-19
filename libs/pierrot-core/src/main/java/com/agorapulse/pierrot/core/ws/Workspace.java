package com.agorapulse.pierrot.core.ws;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class Workspace {

    private static final List<String> IGNORED_DIRECTORIES = List.of(".idea");

    private final File root;

    public Workspace(File root) {
        this.root = root;
    }

    public void visitRepositories(Consumer<LocalRepository> visitor) {
        File[] owners = root.listFiles(File::isDirectory);
        if (owners != null) {
            for (File ownerRoot : owners) {
                if (IGNORED_DIRECTORIES.contains(ownerRoot.getName())) {
                    continue;
                }

                File[] repositories = ownerRoot.listFiles(File::isDirectory);

                if (repositories != null) {
                    for (File repositoryRoot : repositories) {
                        visitor.accept(new LocalRepository(repositoryRoot, ownerRoot.getName() + "/" + repositoryRoot.getName()));
                    }
                }
            }
        }
    }

}
