package com.agorapulse.pierrot

import com.agorapulse.pierrot.core.*
import com.agorapulse.testing.fixt.Fixt
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.stream.Stream

class SearchCommandSpec extends Specification {

    private static final String SEARCH_TERM = 'org:agorapulse filename:.testfile'
    private static final String BRANCH = 'chore/test'
    private static final String TITLE = 'Test Title'
    private static final String MESSAGE = 'Test Message'
    private static final String CONTENT = 'Test Content'
    private static final String PATH = '.testfile'
    private static final String PROJECT = 'Pierrot'
    private static final String REPOSITORY_ONE = 'agorapulse/pierrot'
    private static final String REPOSITORY_TWO = 'agorapulse/oss'

    @AutoCleanup ApplicationContext context

    Fixt fixt = Fixt.create(SearchCommandSpec)

    Repository repository1 = Mock {
        getFullName() >> REPOSITORY_ONE
    }

    Repository repository2 = Mock {
        getFullName() >> REPOSITORY_TWO
    }

    Content content1 = Mock {
        getRepository() >> repository1
        getPath() >> PATH
        getHtmlUrl() >> "https://example.com/$REPOSITORY_ONE/$PATH"
        getTextContent() >> CONTENT
    }

    Content content2 = Mock {
        getRepository() >> repository2
        getHtmlUrl() >> "https://example.com/$REPOSITORY_TWO/$PATH"
        getPath() >> PATH
        getTextContent() >> CONTENT.reverse()
    }

    GitHubService service = Mock {
        searchContent(SEARCH_TERM, false) >> {
            Stream.of(content1, content2)
        }
    }

    void setup() {
        context = ApplicationContext.builder().build()
        context.registerSingleton(GitHubService, service)
        context.start()
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects', 'FactoryMethodName'])
    void 'create remote file'() {
        when:
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            System.out = new PrintStream(baos)

            String[] args = [
                'search',
                '-P',
                SEARCH_TERM,
            ] as String[]
            PicocliRunner.run(PierrotCommand, context, args)

        then:
            baos.toString() == fixt.readText('search.txt')
    }

}
