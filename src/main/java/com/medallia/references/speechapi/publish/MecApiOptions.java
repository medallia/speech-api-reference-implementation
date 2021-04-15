package com.medallia.references.speechapi.publish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

/**
 * Command line options related to the Medallia Speech API.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MecApiOptions {

    @CommandLine.Option(
        names = {"-i", "--client-id"},
        required = true,
        description = "The OAuth 2.0 client id."
    )
    private String clientId;

    @CommandLine.Option(
        names = {"-s", "--client-secret"},
        required = true,
        description = "The OAuth 2.0 client secret."
    )
    private String clientSecret;

    @CommandLine.Option(
        names = {"-t", "--token-url"},
        required = true,
        description = "The OAuth 2.0 token URL."
    )
    private String tokenUrl;

    @CommandLine.Option(
        names = {"-g", "--api-gateway"},
        required = true,
        description = ""
            + "The API gateway URL used to publish records for processing "
            + "in Medallia Speech."
    )
    private String apiEndpointUrl;

}
