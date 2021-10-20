package com.agorapulse.pierrot.core;

import java.io.File;
import java.io.InputStream;

public interface Content {

    String getName();
    String getPath();
    Repository getRepository();
    InputStream getContent();
    String getTextContent();
    String getSha();

    boolean delete(String branchName, String message);
    boolean replace(String branchName, String message, String regexp, String replacement);

    void writeTo(File toPath);

}
