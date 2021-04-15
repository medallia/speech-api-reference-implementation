package com.medallia.references.speechapi.publish;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.RemoveAuthorizedClientReactiveOAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.common.hash.Hashing;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

/**
 * A wrapper service for connecting to MEC using WebClient.  This
 * implementation uses a registry for reusing WebClients assuming none
 * of the identifying information has changed.
 */
@Component
@Slf4j
public class MecWebClientService {

    public static final int MAX_MEMORY_PER_RESPONSE_MB = 20;
    public static final int MAX_MEMORY_PER_RESPONSE_BYTES = MAX_MEMORY_PER_RESPONSE_MB * 1024 * 1024;

    private final Map<String, WebClient> registry = new HashMap<>();

    /**
     * Returns the web client Id for the given parameters.
     *
     * @param tokenUrl tokenUrl
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @return the WebClient
     */
    public String getWebClientId(
            final String tokenUrl,
            final String clientId,
            final String clientSecret
    ) {
        return Hashing.sha256()
            .hashString(
                String.format(
                    "%s|%s|%s",
                    tokenUrl,
                    clientId,
                    clientSecret
                ),
                StandardCharsets.UTF_8
            )
            .toString();
    }

    /**
     * Returns the web client for the given parameters.
     * You must call {@link #getWebClientId} first.
     *
     * @param webClientId webClientId
     * @param tokenUrl tokenUrl
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @return the WebClient
     */
    public WebClient getWebClient(
            final String webClientId,
            final String tokenUrl,
            final String clientId,
            final String clientSecret
    ) {
        synchronized (this.registry) {
            // Populate the registration if this is a new registration id
            if (!this.registry.containsKey(webClientId)) {
                this.registry.put(
                    webClientId,
                    generateWebClient(
                        webClientId,
                        tokenUrl,
                        clientId,
                        clientSecret
                    )
                );
            }

            return this.registry.get(webClientId);
        }
    }

    /**
     * Generates a web client based on the given parameters.
     *
     * @param webClientId registrationId
     * @param tokenUrl tokenUrl
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @return the WebClient
     */
    private WebClient generateWebClient(
            final String webClientId,
            final String tokenUrl,
            final String clientId,
            final String clientSecret
    ) {
        LOGGER.debug("Generating web client for Medallia with OAuth 2.0 support: {}", webClientId);

        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = generateOauthForClientCredentials(
            webClientId,
            tokenUrl,
            clientId,
            clientSecret
        );

        return WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(MAX_MEMORY_PER_RESPONSE_BYTES)
                )
                .build()
            )
            .filter(oauth)
            .build();
    }

    /**
     * Create an OAuth 2.0 client that works with the client credentials grant type.
     *
     * @param webClientId webClientId
     * @param tokenUrl tokenUrl
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @return An OAuth Client Credentials based ExchangeFilterFunction
     */
    public ServerOAuth2AuthorizedClientExchangeFilterFunction generateOauthForClientCredentials(
            final String webClientId,
            final String tokenUrl,
            final String clientId,
            final String clientSecret
    ) {
        final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
            .builder()
            .clientCredentials()
            .build();

        final ReactiveClientRegistrationRepository clientRegistrationRepository =
            new InMemoryReactiveClientRegistrationRepository(ClientRegistration
                .withRegistrationId(webClientId)
                .tokenUri(tokenUrl)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(CLIENT_CREDENTIALS)
                .build()
            );

        final InMemoryReactiveOAuth2AuthorizedClientService clientService =
            new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                clientService
            );

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
            new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        // This allows us to handle expired tokens
        oauth.setAuthorizationFailureHandler(
            new RemoveAuthorizedClientReactiveOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, attributes) -> {
                    final String principalName = Optional.ofNullable(principal)
                        .map(p -> p.getName())
                        .orElse(null);

                    LOGGER.debug(
                        "Removing stale authorization for {} and principal {}",
                        clientRegistrationId,
                        principalName
                    );

                    return clientService.removeAuthorizedClient(
                        clientRegistrationId,
                        principalName
                    );
                }
            )
        );

        return oauth;
    }

}
