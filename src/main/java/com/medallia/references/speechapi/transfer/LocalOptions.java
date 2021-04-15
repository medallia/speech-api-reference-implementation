package com.medallia.references.speechapi.transfer;

import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

/**
* Command line options related to local file transfers.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalOptions {

    @CommandLine.Option(
        names = {"-f", "--folder"},
        required = true,
        description = "Local folder that contains the files to be uploaded."
    )
    private Path folder;

}
