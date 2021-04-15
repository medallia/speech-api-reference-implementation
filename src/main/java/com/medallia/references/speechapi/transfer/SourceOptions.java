package com.medallia.references.speechapi.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

/**
 * Options that relate to the source of the file transfer.  Sources are
 * either an SFTP remote server or a local directory.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceOptions {

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
    private SftpOptions sftp;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
    private LocalOptions local;

}
