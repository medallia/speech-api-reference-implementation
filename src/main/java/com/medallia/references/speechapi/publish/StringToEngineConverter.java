package com.medallia.references.speechapi.publish;

import org.apache.commons.lang3.StringUtils;

import com.medallia.references.speechapi.publish.SpeechRecordMetadata.Engine;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class StringToEngineConverter extends AbstractBeanField<String, Engine> {

    @Override
    protected Object convert(
            final String s
    ) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        for (Engine e : Engine.values()) {
            if (e.getDisplayName().equalsIgnoreCase(s)) {
                return e;
            }
        }

        throw new CsvDataTypeMismatchException(s, Engine.class, "Value did not match expectations");
    }

}
