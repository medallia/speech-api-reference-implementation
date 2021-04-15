package com.medallia.references.speechapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * A Spring Boot wrapper for the application, implementing the command
 * line application.  This interfaces between Spring Boot and Picolo.
 */
@Component
public class ApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final TopMostCommand topMostCommand;

    private final IFactory factory;

    private int exitCode;

    public ApplicationRunner(final TopMostCommand topMostCommand, final IFactory factory) {
        this.topMostCommand = topMostCommand;
        this.factory = factory;
    }

    @Override
    public void run(final String... args) throws Exception {
        exitCode = new CommandLine(topMostCommand, factory)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setUnmatchedArgumentsAllowed(true)
            .execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

}
