package com.medallia.references.speechapi.publish;

import org.apache.commons.lang3.StringUtils;

import com.medallia.references.speechapi.publish.SpeechRecordMetadata.VerticalModel;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class StringToVerticalModelConverter extends AbstractBeanField<String, VerticalModel> {

    @Override
    protected Object convert(
            final String s
    ) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        for (VerticalModel v : VerticalModel.values()) {
            if (v.getDisplayName().equalsIgnoreCase(s)) {
                return v;
            }
        }

        throw new CsvDataTypeMismatchException(s, VerticalModel.class, "Value did not match expectations");
    }

}
