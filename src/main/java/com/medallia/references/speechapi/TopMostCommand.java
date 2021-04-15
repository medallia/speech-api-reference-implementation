package com.medallia.references.speechapi;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.medallia.references.speechapi.publish.PublishCommand;
import com.medallia.references.speechapi.transfer.TransferCommand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

/**
 * This is a wrapper in the Picocli command hierarchy.  It provides for
 * common parameters that influence the subcommands' execution.
 */
@Component
@Command(
    name = "speech",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    scope = ScopeType.INHERIT,
    subcommands = { PublishCommand.class, TransferCommand.class }
)
@Log
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopMostCommand {

    public static final String DEFAULT_NUM_WORKERS = "4";
    public static final String DEFAULT_EXECUTION_TIMEOUT = "1h";
    public static final Integer MAX_WORKERS = 50;
    public static final Pattern EXECUTION_TIMEOUT_PATTERN = Pattern.compile("[ ]*([0-9]+)[ ]*([smhd])[ ]*");

    @Spec
    private CommandSpec spec;

    private Integer numWorkers;
    private Duration executionTimeout;

    /**
     * Sets the number of workers (threads) to be used.
     * @param numWorkers the number of workers to use
     */
    @Option(
        names = {"-p", "--parallel"},
        defaultValue = DEFAULT_NUM_WORKERS,
        required = false,
        description = "The number of concurrent workers to use. (default=${DEFAULT-VALUE})"
    )
    public void setNumWorkers(final Integer numWorkers) {
        if (numWorkers <= 0 || numWorkers > MAX_WORKERS) {
            throw new ParameterException(
                spec.commandLine(),
                String.format(
                    "Invalid concurrency value: must be between 1 and %s (inclusive)",
                    numWorkers,
                    DEFAULT_NUM_WORKERS
                )
            );
        }

        this.numWorkers = numWorkers;
    }

    /**
     * Sets the maximum time that the process is allowed to run.  The value
     * is encoded in a {@code _d_h_m_s} format, such as {@code 5h30m} to
     * refer to 5 hours 30 minutes.
     * @param value the maximum allowed time to run
     */
    @Option(
        names = {"-t", "--timeout"},
        defaultValue = DEFAULT_EXECUTION_TIMEOUT,
        required = false,
        description = "The overall execution timeout. (default=${DEFAULT-VALUE})"
    )
    public void setExecutionTimeout(final String value) {
        try {
            executionTimeout = parseTimeSpecifier(value);
        } catch (DateTimeException e) {
            throw new ParameterException(
                spec.commandLine(),
                String.format("Invalid value for --timeout: %s", value)
            );
        }
    }

    /**
     * Parses the time specifier from {@link #setExecutionTimeout} into a
     * {@link Duration} instance.
     * @param timeSpecifier the time specifier
     * @return the Duration
     */
    private Duration parseTimeSpecifier(final String timeSpecifier) {
        if (timeSpecifier == null) {
            throw new DateTimeException("No time specifier provided");
        }

        final Matcher matcher = EXECUTION_TIMEOUT_PATTERN.matcher(
            timeSpecifier.toLowerCase(Locale.ENGLISH)
        );

        // Count up from 0 (epoch)
        Duration duration = Duration.ZERO;
        while (matcher.find()) {
            final int num = Integer.parseInt(matcher.group(1));
            final String typ = matcher.group(2);

            switch (typ) {
                case "s":
                    duration = duration.plusSeconds(num);
                    break;
                case "m":
                    duration = duration.plusMinutes(num);
                    break;
                case "h":
                    duration = duration.plusHours(num);
                    break;
                case "d":
                    duration = duration.plusDays(num);
                    break;
                default:
                    throw new ParameterException(
                        spec.commandLine(),
                        String.format("Invalid value for --timeout: %s", timeSpecifier)
                    );
            }
        }

        return duration;
    }

}
