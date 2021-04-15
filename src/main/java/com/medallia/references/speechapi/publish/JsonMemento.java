package com.medallia.references.speechapi.publish;

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The memento related to JSON parsing.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonMemento {

    private String dataFilename;
    private InputStream inputStream;
    private JsonParser parser;

}
