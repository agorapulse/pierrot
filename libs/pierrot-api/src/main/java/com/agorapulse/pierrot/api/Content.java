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
package com.agorapulse.pierrot.api;

import java.io.File;
import java.io.InputStream;

public interface Content extends Ignorable {

    String getName();
    String getPath();
    String getHtmlUrl();
    Repository getRepository();
    InputStream getContent();
    String getTextContent();
    String getSha();

    boolean delete(String branchName, String message);
    boolean replace(String branchName, String message, String regexp, String replacement);

    void writeTo(File toPath);


    @Override
    default boolean canBeIgnored() {
        return getRepository().isArchived();
    }

}
