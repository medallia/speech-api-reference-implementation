package com.medallia.references.speechapi.transfer;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

/**
 * Specific options for connections with the Medallia Media File Transfer system.
 * MMFT is an S3-based system.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MmftOptions {

    public static final String DEFAULT_MMFT_FOLDER = "/";

    @CommandLine.Option(
        names = {"--mmft-endpoint"},
        required = true,
        description = "The Medallia Media File Transfer endpoint."
    )
    private String endpoint;

    @CommandLine.Option(
        names = {"--mmft-access-key"},
        required = true,
        description = "The Medallia Media File Transfer access key."
    )
    private String accessKey;

    @CommandLine.Option(
        names = {"--mmft-secret-key"},
        required = true,
        description = "The Medallia Media File Transfer secret key."
    )
    private String secretKey;

    @CommandLine.Option(
        names = {"--mmft-bucket"},
        required = true,
        description = "The Medallia Media File Transfer bucket name."
    )
    private String bucket;

    private String folder;

    @CommandLine.Option(
        names = {"--mmft-folder"},
        defaultValue = DEFAULT_MMFT_FOLDER,
        required = false,
        description = ""
            + "The folder on the Medallia Media File Transfer bucket in which "
            + "to store files."
    )
    public void setFolder(final String folder) {
        this.folder = StringUtils.removeStart(
            StringUtils.removeEnd(folder, "/"),
            "/"
        );
    }

}
