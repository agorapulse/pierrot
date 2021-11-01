package com.agorapulse.pierrot.mixin;

import com.agorapulse.pierrot.core.util.LoggerWithOptionalStacktrace;
import picocli.CommandLine;

public class StacktraceMixin {

    @CommandLine.Option(
        names = {"-s", "--stacktrace"},
        description = "Print stack traces"
    )
    void setStacktrace(boolean stacktrace) {
        if (stacktrace) {
            LoggerWithOptionalStacktrace.enableStacktrace();
        }
    }

}
