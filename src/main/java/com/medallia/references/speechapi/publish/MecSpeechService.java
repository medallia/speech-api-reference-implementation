package com.medallia.references.speechapi.publish;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

/**
 * Wraps the WebClient to publish metadata to the Medallia Speech API.
 */
@Component
@Slf4j
public class MecSpeechService {

    public static final Duration RETRY_BACKOFF_MSECS = Duration.ofMillis(3000);
    public static final Integer RETRY_MAX_ATTEMPTS = 3;

    @Autowired
    private MecWebClientService mecWebClientService;

    public SpeechPublishResults publish(
            final List<SpeechRecordMetadata> page,
            final MecApiOptions mecApi
    ) {
        LOGGER.debug("Publishing data: {}", page);

        final String webClientId = mecWebClientService.getWebClientId(
            mecApi.getTokenUrl(),
            mecApi.getClientId(),
            mecApi.getClientSecret()
        );

        final WebClient webClient = mecWebClientService.getWebClient(
            webClientId,
            mecApi.getTokenUrl(),
            mecApi.getClientId(),
            mecApi.getClientSecret()
        );

        return webClient
            // Build the request
            .post()
            .uri(mecApi.getApiEndpointUrl())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(page)
            // Associate with the dynamic OAuth 2.0 client
            .attributes(clientRegistrationId(webClientId))
            // Get the results from the Medallia Speech API
            .retrieve()
            .onStatus(HttpStatus::isError, errorResponse -> {
                return errorResponse
                    .bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        final String message = String.format(
                            "Error posting data to Speech API (status=%d): %s",
                            errorResponse.rawStatusCode(),
                            errorBody
                        );

                        LOGGER.warn("{}", message);

                        throw new IllegalStateException(message);
                    });
            })
            .bodyToMono(SpeechPublishResults.class)
            // Retry if needed
            .retryWhen(Retry.backoff(RETRY_MAX_ATTEMPTS, RETRY_BACKOFF_MSECS))
            // Block until the call is done
            .block();
    }

}
