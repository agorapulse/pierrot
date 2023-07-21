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
