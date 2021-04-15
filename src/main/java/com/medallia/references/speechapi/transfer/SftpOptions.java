package com.medallia.references.speechapi.transfer;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

/**
 * Command line options related to SFTP.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpOptions {

    public static final String DEFAULT_SFTP_PORT = "22";
    public static final String DEFAULT_SFTP_FOLDER = "/";

    @CommandLine.Option(
        names = {"--sftp-host"},
        required = true,
        description = "The SFTP server host"
    )
    private String host;

    @CommandLine.Option(
        names = {"--sftp-port"},
        defaultValue = DEFAULT_SFTP_PORT,
        required = false,
        description = "The SFTP server port. (default=${DEFAULT-VALUE})"
    )
    private Integer port;

    @CommandLine.Option(
        names = {"--sftp-username"},
        required = true,
        description = "The SFTP server username."
    )
    private String username;

    @CommandLine.Option(
        names = {"--sftp-password"},
        required = true,
        description = "The SFTP server password."
    )
    private String password;

    private String folder;

    @CommandLine.Option(
        names = {"--sftp-folder"},
        defaultValue = DEFAULT_SFTP_FOLDER,
        required = false,
        description = ""
            + "The folder on the SFTP server folder that contains the files "
            + "to be processed. (default=${DEFAULT-VALUE})"
    )
    public void setFolder(final String folder) {
        this.folder = StringUtils.removeStart(
            StringUtils.removeEnd(folder, "/"),
            "/"
        );
    }

}
