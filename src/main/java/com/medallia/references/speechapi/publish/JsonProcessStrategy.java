package com.medallia.references.speechapi.publish;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * The implementation of the metadata publication process as it relates
 * to JSON files as the metadata source.
 */
@Component
@Slf4j
public class JsonProcessStrategy extends GenericProcessStrategy<JsonMemento> {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonProcessStrategy(
            final MecSpeechService mecSpeechService,
            final ObjectMapper objectMapper
    ) {
        super(mecSpeechService);
        this.objectMapper = objectMapper;
    }

    protected Long getNumRecords(final String dataFilename) {
        try (
            InputStream dataFileStream = new FileInputStream(dataFilename);
        ) {
            final JsonFactory factory = objectMapper.getFactory();
            final JsonParser parser = factory.createParser(dataFileStream);

            JsonToken token = parser.nextToken();

            if (token == null) {
                return 0L;
            }

            if (JsonToken.START_OBJECT.equals(token)) {
                return 1L;
            }

            if (JsonToken.START_ARRAY.equals(token)) {
                long numObjects = 0;

                for (token = parser.nextToken(); JsonToken.START_OBJECT.equals(token); token = parser.nextToken()) {
                    // Count the object found
                    numObjects++;

                    // Skip all the object details for now
                    parser.skipChildren();
                }

                return numObjects;
            }

            throw new IllegalStateException("Data file does not contain the expected object or array");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected JsonMemento getMemento(final String dataFilename, final Long numRecords) {
        try {
            final InputStream inputStream = new FileInputStream(dataFilename);

            final JsonMemento memento = JsonMemento.builder()
                .dataFilename(dataFilename)
                .inputStream(inputStream)
                .parser(objectMapper.getFactory().createParser(inputStream))
                .build();

            if (numRecords > 1) {
                // Get past the initial START_ARRAY token
                memento.getParser().nextToken();
            }

            return memento;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<SpeechRecordMetadata> getNextPage(final JsonMemento memento, final Integer batchSize) {
        try {
            LOGGER.debug("Getting next page of records, up to {}", batchSize);

            final List<SpeechRecordMetadata> page = new ArrayList<>();

            int i = 0;
            for (
                JsonToken token = memento.getParser().nextToken();
                i < batchSize && JsonToken.START_OBJECT.equals(token);
                i++, token = memento.getParser().nextToken()
            ) {
                LOGGER.debug("Reading record {}", i);

                final SpeechRecordMetadata record = memento.getParser().readValueAs(SpeechRecordMetadata.class);
                page.add(record);
            }

            return page;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void closeMemento(final JsonMemento memento) {
        try {
            memento.getParser().close();
            memento.getInputStream().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
