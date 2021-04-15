package com.medallia.references.speechapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * The configuration related to Spring Retry.
 */
@Configuration
public class SpringRetryConfig {

    @Bean
    public RetryTemplate retryTemplate(
            @Value("${retry.maxAttempts}") final int maxAttempts,
            @Value("${retry.backoffMultiplier}") final int backoffMultiplier,
            @Value("${retry.initialDelay}") final long initialDelay,
            @Value("${retry.maxDelay}") final long maxDelay
    ) {
        final RetryTemplate retryTemplate = new RetryTemplate();

        final ExponentialRandomBackOffPolicy expRandomBackOffPolicy = new ExponentialRandomBackOffPolicy();
        expRandomBackOffPolicy.setInitialInterval(initialDelay);
        expRandomBackOffPolicy.setMaxInterval(maxDelay);
        expRandomBackOffPolicy.setMultiplier(backoffMultiplier);
        retryTemplate.setBackOffPolicy(expRandomBackOffPolicy);

        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

}
