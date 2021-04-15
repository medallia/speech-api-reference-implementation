package com.medallia.references.speechapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * The entry point of the application.
 */
@SpringBootApplication()
@Slf4j
public class SpeechApiUploadApplication {

    /**
     * The entry point of the application.
     * @param args the command line arguments provided
     */
    public static void main(final String[] args) {
        System.exit(
            SpringApplication.exit(
                SpringApplication.run(
                    SpeechApiUploadApplication.class,
                    args
                )
            )
        );
    }

}
