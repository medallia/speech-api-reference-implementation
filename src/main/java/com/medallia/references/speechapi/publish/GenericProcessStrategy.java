package com.medallia.references.speechapi.publish;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.medallia.references.speechapi.exceptions.ExecutionNotFinishedException;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * The general steps that are needed to publish recording metadata to
 * the Medallia Speech API.  Subclasses provide concrete implementations
 * for getting the number of records to be processed, splitting them
 * into batches, etc.
 * @param <MementoType> the memento holding the state for the process
 */
@Slf4j
public abstract class GenericProcessStrategy<MementoType> {

    public static final int MILLIS_PER_SEC = 1000;

    private final MecSpeechService mecSpeechService;

    protected GenericProcessStrategy(final MecSpeechService mecSpeechService) {
        this.mecSpeechService = mecSpeechService;
    }

    /**
     * The entry point of the processing strategy.
     * @param threads the number of workers to use
     * @param timeout the maximum duration for the execution
     * @param mecApi the options related to the Medallia Speech API
     * @param dataFilename the file that contains the metadata to publish
     * @param batchSize the max number of records per batch
     */
    final void process(
            final Integer threads,
            final Duration timeout,
            final MecApiOptions mecApi,
            final String dataFilename,
            final Integer batchSize
    ) {
        // Step 1: Get metadata about the data file itself and open a
        // memento tracker object so the parser can manage state
        final Long numRecords = getNumRecords(dataFilename);
        final int numBatches = (int) Math.ceil(numRecords.doubleValue() / batchSize.doubleValue());

        System.out.println(String.format(
            "Data file has %d record(s), splitting into %d batch(es)\n",
            numRecords,
            numBatches
        ));

        // Step 2: Setup a progress bar to help show progress
        final ProgressBar progressBar = getProgressBar();
        progressBar.maxHint(numRecords);

        // Step 3: Create a thread pool for parallel execution
        final ExecutorService executorService = Executors.newWorkStealingPool(threads);

        // Step 4: Submit a job to the thread pool for each batch
        final MementoType memento = getMemento(dataFilename, numRecords);

        final List<Future<Void>> tasks = new ArrayList<>(numBatches);

        final AtomicInteger numAccepted = new AtomicInteger(0);
        final AtomicInteger numRejected = new AtomicInteger(0);

        final List<String> errors = new ArrayList<>();

        List<SpeechRecordMetadata> page = null;
        while ((page = getNextPage(memento, batchSize)) != null && !page.isEmpty()) {
            LOGGER.debug("Processing page with {} record(s) in it", page.size());

            // This is needed to allow the lambda expression below to work
            final List<SpeechRecordMetadata> pageFinal = page;

            final Future<Void> task = executorService.submit(() -> {
                final SpeechPublishResults results = mecSpeechService.publish(
                    pageFinal,
                    mecApi
                );

                if (results == null) {
                    throw new IllegalStateException("Received no response from Medallia Speech API");
                }

                switch (results.getJobStatus()) {
                    case ACCEPTED:
                        // Everything in the job was accepted
                        numAccepted.addAndGet(pageFinal.size());
                        break;
                    case REJECTED:
                        // Everything in the job was rejected
                        numRejected.addAndGet(pageFinal.size());

                        if (results.getDetails() == null) {
                            pageFinal.stream().forEach(metadata -> {
                                errors.add(String.format(
                                    "%s: unspecified rejection",
                                    metadata.getSpeechFileName()
                                ));
                            });
                        } else {
                            results.getDetails().stream()
                                .filter(details -> SpeechPublishTaskDetails.TaskStatus.REJECTED.equals(details.getStatus()))
                                .forEach(details -> {
                                    errors.add(String.format(
                                        "%s: %s",
                                        details.getSpeechFileName(),
                                        Optional.ofNullable(details.getErrorMessage())
                                            .orElse("unspecified rejection")
                                    ));
                                });
                        }

                        break;
                    case PARTIALLY_ACCEPTED:
                        if (results.getDetails() == null) {
                            throw new IllegalStateException(
                                "Received no partially accepted details from Medallia Speech API"
                            );
                        }

                        results.getDetails().stream().forEach(details -> {
                            switch (details.getStatus()) {
                                case ACCEPTED:
                                    // This one record was accepted
                                    numAccepted.addAndGet(1);
                                    break;
                                case REJECTED:
                                    // This one record was rejected
                                    numRejected.addAndGet(1);

                                    errors.add(String.format(
                                        "%s: %s",
                                        details.getSpeechFileName(),
                                        Optional.ofNullable(details.getErrorMessage())
                                            .orElse("unspecified rejection")
                                    ));

                                    break;
                                default:
                                    throw new RuntimeException("Unknown details status");
                            }
                        });
                        break;
                    default:
                        throw new RuntimeException("Unknown job status");
                }

                // TODO: filenameStream.println(filename);

                progressBar.stepBy(pageFinal.size());

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

            closeMemento(memento);

            System.out.println();
            System.out.println(String.format("Records accepted: %d", numAccepted.get()));
            System.out.println(String.format("Records rejected: %d", numRejected.get()));
            System.out.println();

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

            if (!errors.isEmpty()) {
                System.out.println();
                errors.stream().forEach(error -> {
                    System.out.println(error);
                });
            }
        }
    }

    private ProgressBar getProgressBar() {
        return new ProgressBarBuilder()
            .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK) // ASCII?
            .setTaskName("Publishing")
            .showSpeed()
            .build();
    }

    protected abstract Long getNumRecords(String dataFilename);

    protected abstract MementoType getMemento(String dataFilename, Long numRecords);

    protected abstract List<SpeechRecordMetadata> getNextPage(MementoType memento, Integer batchSize);

    protected abstract void closeMemento(MementoType memento);

}
