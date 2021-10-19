package com.agorapulse.pierrot;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.io.Console;

@Factory
public class ConsoleFactory {

    @Bean
    @Singleton
    public Console console() {
        return System.console();
    }

}
