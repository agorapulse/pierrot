/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Vladimir Orany.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.Repository;
import com.agorapulse.pierrot.core.util.LoggerWithOptionalStacktrace;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DefaultContent implements Content {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(DefaultContent.class);

    private final GHContent content;
    private final Repository repositoryWrapper;

    public DefaultContent(GHContent content, GHRepository repository, GHUser myself, GitHubConfiguration configuration) {
        this.content = content;
        this.repositoryWrapper = new DefaultRepository(repository, myself, configuration);
    }

    @Override
    public String getName() {
        return content.getName();
    }

    @Override
    public String getPath() {
        return content.getPath();
    }

    @Override
    public String getHtmlUrl() {
        return content.getHtmlUrl();
    }

    @Override
    public Repository getRepository() {
        return repositoryWrapper;
    }

    @Override
    public InputStream getContent() {
        try {
            return content.read();
        } catch (IOException e) {
            LOGGER.error("Exception fetching content of " + getRepository().getFullName() + "/" + getPath(), e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public String getTextContent() {
        try {
            return new String(getContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Exception fetching text content of " + getRepository().getFullName() + "/" + getPath(), e);
            return "";
        }
    }

    @Override
    public String getSha() {
        return content.getSha();
    }

    @Override
    public boolean delete(String branchName, String message) {
        try {
            content.delete(message, branchName);
            return true;
        } catch (GHFileNotFoundException e) {
            LOGGER.info("File {}/{} no longer exists", getRepository().getFullName(), getPath());
            return false;
        } catch (IOException e) {
            LOGGER.error("Exception deleting " + getRepository().getFullName() + "/" + getPath(), e);
            return false;
        }
    }

    @Override
    public boolean replace(String branchName, String message, String regexp, String replacement) {
        try {
            String text = getTextContent();
            String newText = text.replaceAll(regexp, replacement);

            if (newText.equals(text)) {
                LOGGER.info("The content of {} is still the same after replacement", getPath());
                return false;
            }

            content.update(newText, message, branchName);
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception updating " + getRepository().getFullName() + "/" + getPath(), e);
        }
        return false;
    }

    @Override
    public void writeTo(File location) {
        location.getParentFile().mkdirs();

        try (OutputStream outStream = new FileOutputStream(location)) {
            outStream.write(getContent().readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
