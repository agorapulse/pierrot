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
package com.agorapulse.pierrot.cli

import com.agorapulse.pierrot.api.CheckRun
import com.agorapulse.pierrot.api.Content
import com.agorapulse.pierrot.api.GitHubService
import com.agorapulse.pierrot.api.Project
import com.agorapulse.pierrot.api.PullRequest
import com.agorapulse.pierrot.api.Repository
import com.agorapulse.testing.fixt.Fixt
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.util.stream.Stream

@SuppressWarnings('UnnecessaryGetter')
abstract class AbstractCommandSpec extends Specification {

    public static final String OWNER = 'agorapulse'
    public static final String CONTENT_SEARCH_TERM_1 = 'org:agorapulse'
    public static final String CONTENT_SEARCH_TERM_2 = 'filename:.testfile'
    public static final String CONTENT_SEARCH_TERM_3 = 'hello world'
    public static final String CONTENT_SEARCH_TERM_EXPECTED = "$CONTENT_SEARCH_TERM_1 $CONTENT_SEARCH_TERM_2 \"$CONTENT_SEARCH_TERM_3\""
    public static final String BRANCH = 'chore/test'
    public static final String TITLE = 'Test Title'
    public static final String MESSAGE = 'Test Message'
    public static final String CONTENT = 'Test Content'
    public static final String PATH = '.testfile'
    public static final String PROJECT = 'Pierrot'
    public static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    public static final String REPOSITORY_TWO = 'agorapulse/oss'
    public static final String PR_SEARCH_TERM = 'Agorapulse BOM'
    public static final String REPLACEMENT = 'salut $1'
    public static final String PATTERN = /hello (\w+)/

    @TempDir File workspace

    @Shared Fixt fixt = Fixt.create(getClass())

    @AutoCleanup ApplicationContext context

    abstract List<String> getArgs()

    Project project = Mock {
        getName() >> PROJECT
        getHttpUrl() >> new URL("https://example.com/$OWNER/projects/1")
    }

    CheckRun run1 = Mock {
        getName() >> 'Check 1'
        getStatus() >> 'completed'
        getConclusion() >> 'success'
    }

    CheckRun run2 = Mock {
        getName() >> 'Check 2'
        getStatus() >> 'completed'
        getConclusion() >> 'failure'
    }

    CheckRun run3 =  Mock {
        getName() >> 'Check 3'
        getStatus() >> 'completed'
        getConclusion() >> 'unknown'
    }

    CheckRun run4 =  Mock {
        getName() >> 'Check 4'
        getStatus() >> 'pending'
    }

    PullRequest pullRequest1 = Mock {
        getTitle() >> 'Test PR 1'
        isMerged() >> true
        getMergeableState() >> 'unknown'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_ONE/pulls/1")
        getChecks() >> Stream.of(run1, run2)
    }

    PullRequest pullRequest2 = Mock {
        getTitle() >> 'Test PR 2'
        getMergeableState() >> 'unstable'
        getHtmlUrl() >> new URL("https://example.com/$REPOSITORY_TWO/pulls/1")
        getChecks() >> Stream.of(run3, run4)
    }

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
        canWrite() >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> true
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest1)
        getOwnerName() >> OWNER
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        canWrite() >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> false
        writeFile(BRANCH, MESSAGE, PATH, CONTENT.reverse()) >> false
        createPullRequest(BRANCH, TITLE, MESSAGE) >> Optional.of(pullRequest2)
        getOwnerName() >> OWNER
    }

    Content content1 = Mock {
        getRepository() >> repository1
        delete(BRANCH, MESSAGE) >> true
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> true
        getPath() >> PATH
        getHtmlUrl() >> "https://example.com/$REPOSITORY_ONE/$PATH"
        getTextContent() >> CONTENT
        writeTo(_ as File) >> { File f ->
            f.parentFile.mkdirs()
            f.createNewFile()
            f.write(CONTENT)
        }
    }

    Content content2 = Mock {
        getRepository() >> repository2
        delete(BRANCH, MESSAGE) >> true
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> true
        getPath() >> PATH
        getHtmlUrl() >> "https://example.com/$REPOSITORY_TWO/$PATH"
        getTextContent() >> CONTENT.reverse()
        writeTo(_ as File) >> { File f ->
            f.parentFile.mkdirs()
            f.createNewFile()
            f.write(CONTENT.reverse())
        }
    }

    Content content3 = Mock {
        getRepository() >> repository2
        delete(BRANCH, MESSAGE) >> false
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> false
        getPath() >> "prefix/$PATH"
        getHtmlUrl() >> "https://example.com/$REPOSITORY_TWO/prefix/$PATH"
        getTextContent() >> CONTENT
        writeTo(_ as File) >> { File f ->
            f.parentFile.mkdirs()
            f.createNewFile()
            f.write(CONTENT)
        }
    }

    GitHubService service = Mock {
        getRepository(REPOSITORY_ONE) >> Optional.of(repository1)
        getRepository(REPOSITORY_TWO) >> Optional.of(repository2)

        searchContent(CONTENT_SEARCH_TERM_EXPECTED, false) >> {
            Stream.of(content1, content2, content3)
        }

        searchPullRequests("\"$PR_SEARCH_TERM\"".toString(), true, false) >> {
            Stream.of(pullRequest1, pullRequest2)
        }

        findOrCreateProject(OWNER, PROJECT, _ as String) >> Optional.of(project)
        findProject(OWNER, PROJECT) >> Optional.of(project)
    }

    void setup() {
        context = ApplicationContext.builder(Environment.CLI, Environment.TEST).build()
        context.registerSingleton(GitHubService, service)
        context.start()

        pullRequest1.getRepository() >> repository1
        pullRequest2.getRepository() >> repository2
    }

    void 'display help'() {
        expect:
            runCommand('help.txt', getHelpArgs())
    }

    void 'run command'() {
        expect:
            runCommand('run.txt', args) { additionalChecks() }
    }

    protected boolean runCommand(String referenceFileName, List<String> input = [], List<String> args) {
        return runCommand(referenceFileName, input, args) { }
    }

    @SuppressWarnings(['ConstantAssertExpression', 'Println'])
    protected boolean runCommand(String referenceFileName, List<String> input = [], List<String> args, Runnable additionalChecks) {
        TestConsole console = TestConsole.capture(input.join(System.lineSeparator())) {
            PicocliRunner.run(PierrotCommand, context, args as String[])
        }

        assert !console.err

        String content = fixt.readText(referenceFileName)

        if (!content) {
            fixt.writeText(referenceFileName, console.out)
            assert false, "New file $referenceFileName has been generated. Please, run the test again!"
        }

        println('=' * 100)
        println "EXPECTED:\n$content"
        println('-' * 100)
        println "ACTUAL:\n$console.out"

        assert console.out == expandFile(content)

        additionalChecks()

        return true
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects', 'FactoryMethodName'])
    protected File createWorkspaceFile(String repositoryFullName, String path, String content) {
        File file = new File(workspace, "$repositoryFullName/$path")
        file.parentFile.mkdirs()
        file.createNewFile()
        file.write(content)
        return file
    }

    protected String expandFile(String input) {
        return input
    }

    protected boolean additionalChecks() {
        return true
    }

    protected List<String> getHelpArgs() {
        return [args.first(), '--help']
    }

}
