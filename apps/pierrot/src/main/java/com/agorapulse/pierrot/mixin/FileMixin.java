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
package com.agorapulse.pierrot.mixin;

import com.agorapulse.pierrot.core.util.LazyLogger;
import io.micronaut.core.util.StringUtils;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class FileMixin {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LazyLogger.create(FileMixin.class);

    private final Scanner scanner = new Scanner(System.in);

    private UnaryOperator<String> reader = s -> {
        System.out.print(s);
        return scanner.nextLine();
    };

    private Consumer<String> writer = System.out::println;

    @CommandLine.Option(
        names = {"-p", "--path"},
        description = "The path inside the repository"
    )
    String path;

    @CommandLine.Option(
        names = {"-c", "--content"},
        description = "The inline content of the newly created file"
    )
    String content;

    @CommandLine.Option(
        names = {"--from"},
        description = "The file to be uploaded to the repositories"
    )
    File contentFrom;

    public FileMixin() { }

    public FileMixin(String path, String content, File contentFrom) {
        this.path = path;
        this.content = content;
        this.contentFrom = contentFrom;
    }

    public FileMixin(UnaryOperator<String> reader, Consumer<String> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public String readPath() {
        if (StringUtils.isEmpty(path) && contentFrom != null && contentFrom.exists()) {
            return contentFrom.getName();
        }

        while (StringUtils.isEmpty(path)) {
            path = reader.apply("Path: ");
        }

        return path;
    }

    public String readContent() {
        // it's more convenient for the user to always ask for the path first, before the content
        readPath();

        if (StringUtils.isNotEmpty(content)) {
            return content;
        }

        if (contentFrom != null && contentFrom.exists()) {
            try {
                return Files.readString(contentFrom.toPath());
            } catch (IOException e) {
                LOGGER.error("Exception reading content of " + contentFrom, e);
            }
        }

        while (StringUtils.isEmpty(content)) {
            writer.accept("Content (use triple new line to submit):%n");

            StringWriter stringWriter = new StringWriter();

            int emptyLines = 0;

            while (emptyLines < 2) {
                String line = reader.apply("> ");

                if (StringUtils.isEmpty(line)) {
                    emptyLines++;
                } else {
                    emptyLines = 0;
                }

                stringWriter.append(line);
            }

            content = stringWriter.toString();
        }

        return content;
    }

}
