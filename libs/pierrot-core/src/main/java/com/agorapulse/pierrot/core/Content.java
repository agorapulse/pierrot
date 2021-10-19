package com.agorapulse.pierrot.core;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface Content {

    String getName();
    String getPath();
    Repository getRepository();
    InputStream getContent();
    String getTextContent();
    String getSha();

    void delete(String branchName, String message);
    void update(String branchName, String message, String regexp, String replacement);

    void writeTo(File toPath);

}
