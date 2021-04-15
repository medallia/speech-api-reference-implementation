package com.medallia.references.speechapi.transfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
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
 * The Picocli command that implements a file transfer to the Medallia
 * Media File Transfer server.
 */
@Component
@Command(
    name = "transfer",
    mixinStandardHelpOptions = true
)
@Log
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferCommand implements Callable {

    public static final String DEFAULT_GLOB_PATTERN = "*";

    @Autowired
    private SftpProcessStrategy sftpProcessStrategy;

    @Autowired
    private LocalProcessStrategy localProcessStrategy;

    @ParentCommand
    private TopMostCommand parent;

    @Spec
    private CommandSpec spec;

    @ArgGroup(exclusive = true, multiplicity = "1")
    private SourceOptions source;

    @ArgGroup(exclusive = false, multiplicity = "1")
    private MmftOptions mmft;

    @CommandLine.Option(
        names = {"-g", "--glob"},
        defaultValue = DEFAULT_GLOB_PATTERN,
        required = false,
        description = ""
            + "The glob pattern to use for selecting files to transfer. "
            + "(default=${DEFAULT-VALUE})"
    )
    private String globPattern;

    @CommandLine.Option(
        names = {"-o", "--output"},
        defaultValue = "",
        required = false,
        description = ""
            + "Writes the filenames to the output file (if set). "
            + "(default=disabled)"
    )
    private String outputFilename;

    private GenericProcessStrategy getProcessStrategy() {
        if (source.getLocal() != null) {
            return localProcessStrategy;
        }

        if (source.getSftp() != null) {
            return sftpProcessStrategy;
        }

        throw new IllegalStateException("Must set source type as either local or SFTP");
    }

    @Override
    public Integer call() {
        // Create the process strategy that will be used to execute the job
        final GenericProcessStrategy strategy = getProcessStrategy();

        // Create a file stream that can be used to output filenames
        // to a text file as a log
        final PrintStream filenameStream = Optional
            .ofNullable(StringUtils.isNotBlank(outputFilename) ? outputFilename : "/dev/null")
            .map(filename -> {
                try {
                    return new PrintStream(new FileOutputStream(new File(filename)));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            })
            .get();

        // Initiate the transfer process
        strategy.process(
            parent.getNumWorkers(),
            parent.getExecutionTimeout(),
            source,
            mmft,
            globPattern,
            filenameStream
        );

        filenameStream.close();

        return 0;
    }

}
