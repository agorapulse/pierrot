/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Vladimir Orany.
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
package com.agorapulse.pierrot.cli.summary;

import com.agorapulse.pierrot.api.summary.SummaryCollector;
import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;

@Singleton
@Requires(property = "summary.file", bean = SummaryCollector.class)
public class SummaryWriter {

    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(SummaryWriter.class);

    private final String summaryFilePath;
    private final SummaryCollector collector;

    public SummaryWriter(@Value("${summary.file}") String summaryFilePath, SummaryCollector collector) {
        this.summaryFilePath = summaryFilePath;
        this.collector = collector;
    }

    public File write() {
        try {
            File file = new File(summaryFilePath);
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), collector.getSummary());
            return file;
        } catch (Exception e) {
            LOGGER.error("Cannot write summary to " + summaryFilePath, e);
            return null;
        }
    }
}
