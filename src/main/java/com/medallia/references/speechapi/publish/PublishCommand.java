package com.medallia.references.speechapi.publish;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.medallia.references.speechapi.TopMostCommand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

/**
 * The Picocli command that implements metadata publication to the
 * Medallia Speech API.
 */
@Component
@Command(
    name = "publish",
    mixinStandardHelpOptions = true
)
@Log
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishCommand implements Callable {

    public static final String DEFAULT_BATCH_SIZE = "1000";

    @Autowired
    private JsonProcessStrategy jsonProcessStrategy;

    @Autowired
    private CsvProcessStrategy csvProcessStrategy;

    @ParentCommand
    private TopMostCommand parent;

    @Spec
    private CommandSpec spec;

    @ArgGroup(exclusive = false, multiplicity = "1")
    private MecApiOptions mecApi;

    @CommandLine.Option(
        names = {"-d", "--data"},
        required = true,
        description = ""
            + "The metadata to upload to Medallia Speech for processing. "
            + "CSV or JSON format are accepted and auto-detected based on "
            + "the file's MIME type."
    )
    private String dataFilename;

    @CommandLine.Option(
        names = {"-b", "--batch-size"},
        defaultValue = DEFAULT_BATCH_SIZE,
        required = false,
        description = ""
            + "The number of records to publish in a single batch. "
            + "(default=${DEFAULT-VALUE})"
    )
    private Integer batchSize;

    private GenericProcessStrategy getProcessStrategy() {
        try {
            final File dataFile = new File(dataFilename);
            final Tika tika = new Tika();
            final String mimeType = tika.detect(dataFile);

            switch (mimeType) {
                case "text/csv":
                    return csvProcessStrategy;
                case "application/json":
                    return jsonProcessStrategy;
                default:
                    throw new IllegalStateException(String.format(
                        "Unable to use data file of type %s",
                        mimeType
                    ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer call() {
        // Create the process strategy that will be used to execute the job
        final GenericProcessStrategy strategy = getProcessStrategy();

        strategy.process(
            parent.getNumWorkers(),
            parent.getExecutionTimeout(),
            mecApi,
            dataFilename,
            batchSize
        );

        return 0;
    }

}
