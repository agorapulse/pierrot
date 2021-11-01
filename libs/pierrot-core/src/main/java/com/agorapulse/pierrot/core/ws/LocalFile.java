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
package com.agorapulse.pierrot.core.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LocalFile {

    // the field is not static to prevent GraalVM FileAppender issues
    private final Logger LOGGER = LoggerFactory.getLogger(LocalFile.class);

    private final File location;
    private final String path;

    public LocalFile(File location, String path) {
        this.location = location;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        try {
            return Files.readString(location.toPath());
        } catch (IOException e) {
            LOGGER.error("Exception reading text content of the file " + getPath(), e);
            return "";
        }
    }

}
