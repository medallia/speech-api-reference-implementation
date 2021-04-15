package com.medallia.references.speechapi.transfer;

import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;
import com.medallia.references.speechapi.exceptions.ExecutionNotFinishedException;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * The general steps that are needed to transfer a file to the Medallia
 * Media File Transfer server.  Subclasses provide concrete implementations
 * for getting the list of files, the file data, etc.
 */
public abstract class GenericProcessStrategy {

    public static final int MILLIS_PER_SEC = 1000;

    private final MmftService mmftService;

    protected GenericProcessStrategy(final MmftService mmftService) {
        this.mmftService = mmftService;
    }

    /**
     * The entry point of the processing strategy.
     * @param threads the number of workers to use
     * @param timeout the maximum duration for the execution
     * @param source the options related to the file source
     * @param mmft the options related to MMFT
     * @param globPattern the glob pattern to use for filtering source files
     * @param filenameStream the stream for printing processed filenames
     */
    final void process(
            final Integer threads,
            final Duration timeout,
            final SourceOptions source,
            final MmftOptions mmft,
            final String globPattern,
            final PrintStream filenameStream
    ) {
        // Step 1: Get the list of filenames from the source
        System.out.println("Getting a list of filenames from the source");

        final MatchingEngine globMatchingEngine = GlobPattern.compile(globPattern);

        final Collection<String> filenames = getFilenames(source)
            .stream()
            .filter(filename -> globMatchingEngine.matches(filename))
            .collect(Collectors.toSet());

        System.out.println(String.format("Found %d file(s) to process", filenames.size()));

        // Step 2: Setup a progress bar to help show progress
        final ProgressBar progressBar = getProgressBar();
        progressBar.maxHint(filenames.size());

        // Step 3: Create a thread pool for parallel execution
        final ExecutorService executorService = Executors.newWorkStealingPool(threads);

        // Step 4: Submit a job to the thread pool for each filename
        final List<Future<Void>> tasks = new ArrayList<>(filenames.size());

        for (String filename : filenames) {
            final Future<Void> task = executorService.submit(() -> {
                mmftService.upload(
                    filename,
                    getBytesFromSource(filename, source),
                    mmft
                );

                filenameStream.println(filename);

                progressBar.stepBy(1);

                return null;
            });

            tasks.add(task);
        }

        try {
            // Trigger a shutdown, which is blocked until the worker threads
            // terminate or the executor service interrupts due to a timeout.
            executorService.shutdown();

            // Setup the timeout
            final boolean finished = executorService.awaitTermination(
                timeout.getSeconds(),
                TimeUnit.SECONDS
            );

            if (!finished) {
                throw new ExecutionNotFinishedException(String.format(
                    "Process exceeded max allowed time of %s",
                    DurationFormatUtils.formatDuration(
                        timeout.getSeconds() * MILLIS_PER_SEC,
                        "dHms"
                    )
                ));
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw new ExecutionNotFinishedException("The process was interrupted", e);
        } finally {
            progressBar.close();

            tasks.forEach(task -> {
                try {
                    // Calling "get()" will propagate the thread's thrown exceptions.
                    // Do this at the end to not block the parallel executions.
                    task.get();
                } catch (InterruptedException | ExecutionException e) {
                    final String errorMessage = String.format("There was some unexpected problem: %s", e.getMessage());
                    throw new ExecutionNotFinishedException(errorMessage, e);
                }
            });
        }
    }

    private ProgressBar getProgressBar() {
        return new ProgressBarBuilder()
            .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK) // ASCII?
            .setTaskName("Transferring")
            .showSpeed()
            .build();
    }

    protected abstract Collection<String> getFilenames(SourceOptions source);

    protected abstract byte[] getBytesFromSource(
        String filename,
        SourceOptions source
    );

}
