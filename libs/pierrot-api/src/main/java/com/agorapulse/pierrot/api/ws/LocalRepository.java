/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2022 Vladimir Orany.
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
package com.agorapulse.pierrot.api.ws;

import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LocalRepository {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(LocalRepository.class);

    private final File location;
    private final String name;

    public LocalRepository(File location, String name) {
        this.location = location;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void visitFiles(Consumer<LocalFile> visitor) {
        try (Stream<Path> files = Files.find(location.toPath(), Integer.MAX_VALUE, (f, attrs) -> Files.isRegularFile(f))) {
            files.forEach(f -> {
                try {
                    String path = f.toFile().getCanonicalPath().substring(location.getCanonicalPath().length() + 1);
                    visitor.accept(new LocalFile(f.toFile(), path));
                } catch (IOException e) {
                    LOGGER.error("Exception extracting path from" + f, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Exception walking repository " + getName(), e);
        }
    }

}
