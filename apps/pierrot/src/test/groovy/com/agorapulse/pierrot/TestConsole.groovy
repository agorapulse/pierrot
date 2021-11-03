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
package com.agorapulse.pierrot

import groovy.transform.CompileStatic

@CompileStatic
class TestConsole {

    final String out
    final String err

    TestConsole(String out, String err) {
        this.out = out
        this.err = err
    }

    static TestConsole capture(String input = '', Runnable action) {
        PrintStream originalOut = System.out
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        System.out = new PrintStream(bout)

        PrintStream originalErr = System.err
        ByteArrayOutputStream berr = new ByteArrayOutputStream()
        System.err = new PrintStream(berr)

        InputStream originalIn = System.in
        ByteArrayInputStream bais = new ByteArrayInputStream(input.bytes)
        System.in = bais

        try {
            action.run()
        } finally {
            System.out = originalOut
            System.err = originalErr
            System.in = originalIn
        }

        return new TestConsole(bout.toString(), berr.toString())
    }

}
