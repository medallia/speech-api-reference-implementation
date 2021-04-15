package com.medallia.references.speechapi.publish;

import org.apache.commons.lang3.StringUtils;

import com.medallia.references.speechapi.publish.SpeechRecordMetadata.AgentChannelInRecording;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class StringToAgentChannelInRecordingConverter extends AbstractBeanField<String, AgentChannelInRecording> {

    @Override
    protected Object convert(
            final String s
    ) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        for (AgentChannelInRecording a : AgentChannelInRecording.values()) {
            if (a.getDisplayName().equalsIgnoreCase(s)) {
                return a;
            }
        }

        throw new CsvDataTypeMismatchException(s, AgentChannelInRecording.class, "Value did not match expectations");
    }

}
