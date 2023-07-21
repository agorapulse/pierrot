/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Vladimir Orany.
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
import com.agorapulse.pierrot.api.event.ContentUpdatedEvent
import com.agorapulse.pierrot.api.event.ProjectCreatedEvent
import com.agorapulse.pierrot.api.event.PullRequestAddedToProjectEvent
import com.agorapulse.pierrot.api.event.PullRequestCreatedEvent
import com.agorapulse.pierrot.api.event.UpdateType
import com.agorapulse.pierrot.cli.summary.SummaryWriter
import com.agorapulse.testing.fixt.Fixt
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventPublisher
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

    List<Object> events = []

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
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> {
            events << new ContentUpdatedEvent(content1, UpdateType.CREATED)
            true
        }
        createPullRequest(BRANCH, TITLE, MESSAGE) >> {
            events << new PullRequestCreatedEvent(pullRequest1)
            events << new PullRequestAddedToProjectEvent(project, pullRequest1)
            Optional.of(pullRequest1)
        }
        getOwnerName() >> OWNER
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
        canWrite() >> true
        writeFile(BRANCH, MESSAGE, PATH, CONTENT) >> false
        writeFile(BRANCH, MESSAGE, PATH, CONTENT.reverse()) >> false
        createPullRequest(BRANCH, TITLE, MESSAGE) >> {
            events << new PullRequestCreatedEvent(pullRequest2)
            events << new PullRequestAddedToProjectEvent(project, pullRequest2)
            Optional.of(pullRequest2)
        }
        getOwnerName() >> OWNER
    }

    Content content1 = Mock {
        getRepository() >> repository1
        delete(BRANCH, MESSAGE) >> {
            events << new ContentUpdatedEvent(content1, UpdateType.DELETED)
            true
        }
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> {
            events << new ContentUpdatedEvent(content1, UpdateType.UPDATED)
            true
        }
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
        delete(BRANCH, MESSAGE) >> {
            events << new ContentUpdatedEvent(content2, UpdateType.DELETED)
            true
        }
        replace(BRANCH, MESSAGE, PATTERN, REPLACEMENT) >> {
            events << new ContentUpdatedEvent(content2, UpdateType.UPDATED)
            true
        }
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

        findOrCreateProject(OWNER, PROJECT, _ as String) >> {
            events << new ProjectCreatedEvent(project)
            Optional.of(project)
        }
        findProject(OWNER, PROJECT) >> {
            events << new ProjectCreatedEvent(project)
            Optional.of(project)
        }
    }

    void setup() {
        context = ApplicationContext
            .builder(Environment.CLI, Environment.TEST)
            .properties(
                'summary.file': new File(workspace, 'summary.txt').canonicalPath
            )
            .build()
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

        when:
            events.forEach { event ->
                context.getBean(ApplicationEventPublisher).publishEvent(event)
            }
            context.getBean(SummaryWriter).write()
        then:
            compareOutput('summary.txt', new File(workspace, 'summary.txt').text, args)
    }

    protected boolean runCommand(String referenceFileName, List<String> input = [], List<String> args) {
        return runCommand(referenceFileName, input, args) { }
    }

    protected boolean runCommand(String referenceFileName, List<String> input = [], List<String> args, Runnable additionalChecks) {
        TestConsole console = TestConsole.capture(input.join(System.lineSeparator())) {
            PicocliRunner.run(PierrotCommand, context, args as String[])
        }

        assert !console.err

        compareOutput(referenceFileName, console, args)

        additionalChecks()

        return true
    }

    @SuppressWarnings(['Println', 'ConstantAssertExpression'])
    protected void compareOutput(String referenceFileName, TestConsole console, List<String> args) {
        compareOutput(referenceFileName, console.out, args)
    }

    @SuppressWarnings(['Println', 'ConstantAssertExpression'])
    protected void compareOutput(String referenceFileName, String text, List<String> args) {
        String content = fixt.readText(referenceFileName)

        if (!content) {
            fixt.writeText(referenceFileName, text)
            assert false, "New file $referenceFileName has been generated. Please, run the test again!"
        }

        println('=' * 100)
        println('| ' + "pierrot ${args.join()}".center(96, ' ') + ' |')
        println('=' * 100)
        println content
        println(' ACTUAL '.center(100, '-'))
        println text

        assert text == expandFile(content)
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
