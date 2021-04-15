package com.medallia.references.speechapi.publish;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class StringToMapConverter extends AbstractBeanField<String, Map<String, String>> {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected Object convert(
            final String s
    ) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(
                s,
                new TypeReference<HashMap<String, String>>() {
                    // Nothing to do
                }
            );
        } catch (IOException e) {
            throw new CsvDataTypeMismatchException(s, Map.class);
        }
    }

}
