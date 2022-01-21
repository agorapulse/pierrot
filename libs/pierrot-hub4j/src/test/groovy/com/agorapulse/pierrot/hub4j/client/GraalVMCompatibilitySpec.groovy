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
package com.agorapulse.pierrot.hub4j.client

import com.agorapulse.pierrot.hub4j.DefaultGitHubService
import io.micronaut.core.annotation.TypeHint
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import spock.lang.Specification

class GraalVMCompatibilitySpec extends Specification {

    void 'scan all github model classes'() {
        when:
            Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                    .forPackage('org.kohsuke.github')
                    .filterInputsBy(type -> type.startsWith('org.kohsuke.github'))
                    .addScanners(Scanners.SubTypes.filterResultsBy {true })
            )

            List<String> classes = reflections.allTypes.findAll {
                it.startsWith 'org.kohsuke.github.GH'
            } collectMany { String typeName ->
                getAllClasses(Class.forName(typeName))
            } sort false unique false

            printAnnotation 'FOUND', classes

            List<String> declared = DefaultGitHubService.getAnnotation(TypeHint).typeNames().sort(false).toUnique().toList()

            printAnnotation 'DECLARED', declared

            Set<String> extra = declared - classes
            Set<String> missing = (classes - declared).findAll { !it.matches(/.*\d+/) }
        then:
            extra.size() == 0
            missing.size() == 0
    }

    private List<String> getAllClasses(Class<?> clazz) {
        List<String> ret = [clazz.name]
        ret.addAll(
            clazz.declaredClasses.collectMany { Class<?> declared ->
                getAllClasses(declared)
            }
        )
        return ret
    }

    private static void printAnnotation(String headline, Iterable<String> classes) {
        println()
        println headline
        println """
            @TypeHint(
                typeNames = {${classes.collect { "\n                    \"$it\"," }.join() }
                },
                accessType = {
                    ALL_PUBLIC,
                    ALL_DECLARED_CONSTRUCTORS,
                    ALL_PUBLIC_CONSTRUCTORS,
                    ALL_DECLARED_METHODS,
                    ALL_DECLARED_FIELDS,
                    ALL_PUBLIC_METHODS,
                    ALL_PUBLIC_FIELDS
                }
            )
            """.stripIndent()
    }

}
