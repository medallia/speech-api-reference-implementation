package com.medallia.references.speechapi.publish;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class EmptyStringToNullStringConverter extends AbstractBeanField<String, String> {

    @Override
    protected Object convert(
            final String s
    ) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        return StringUtils.isBlank(s) ? null : s;
    }

}
