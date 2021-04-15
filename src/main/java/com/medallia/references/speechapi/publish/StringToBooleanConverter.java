package com.medallia.references.speechapi.publish;

import org.apache.commons.lang3.StringUtils;

import com.medallia.references.speechapi.publish.SpeechRecordMetadata.BooleanString;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class StringToBooleanConverter extends AbstractBeanField<String, BooleanString> {

    @Override
    protected Object convert(
            final String s
    ) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        for (BooleanString b : BooleanString.values()) {
            if (b.getDisplayName().equalsIgnoreCase(s)) {
                return b;
            }
        }

        throw new CsvDataTypeMismatchException(s, BooleanString.class, "Value did not match expectations");
    }

}
