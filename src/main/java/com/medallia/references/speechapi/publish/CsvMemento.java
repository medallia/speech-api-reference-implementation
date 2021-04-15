package com.medallia.references.speechapi.publish;

import java.util.Iterator;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The memento related to CSV parsing.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvMemento {

    private CSVReader csvReader;
    private CsvToBean csvToBean;
    private Iterator<SpeechRecordMetadata> csvToBeanIterator;

}
