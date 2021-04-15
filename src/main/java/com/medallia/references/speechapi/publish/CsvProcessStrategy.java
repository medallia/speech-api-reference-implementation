package com.medallia.references.speechapi.publish;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
// import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * The implementation of the metadata publication process as it relates
 * to CSV files as the metadata source.
 */
@Component
@Slf4j
public class CsvProcessStrategy extends GenericProcessStrategy<CsvMemento> {

    @Autowired
    public CsvProcessStrategy(
            final MecSpeechService mecSpeechService
    ) {
        super(mecSpeechService);
    }

    protected Long getNumRecords(final String dataFilename) {
        try (
            CSVReader csvReader = new CSVReader(new FileReader(dataFilename))
        ) {
            // Skip the header row by using some math trickery
            long numLines = -1;
            while (csvReader.readNext() != null) {
                numLines++;
            }
            return numLines;
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    protected CsvMemento getMemento(final String dataFilename, final Long numRecords) {
        try {
            final CSVReader csvReader = new CSVReader(new FileReader(dataFilename));

            // final HeaderColumnNameMappingStrategy<SpeechRecordMetadata> mappingStrategy =
            //     new HeaderColumnNameMappingStrategy<>();
            //
            // mappingStrategy.setType(SpeechRecordMetadata.class);

            final CsvToBean<SpeechRecordMetadata> csvToBean = new CsvToBeanBuilder<SpeechRecordMetadata>(csvReader)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                // .withMappingStrategy(mappingStrategy)
                .withOrderedResults(true)
                .withType(SpeechRecordMetadata.class)
                .build();

            final Iterator<SpeechRecordMetadata> csvToBeanIterator = csvToBean.iterator();

            return CsvMemento.builder()
                .csvReader(csvReader)
                .csvToBean(csvToBean)
                .csvToBeanIterator(csvToBeanIterator)
                .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<SpeechRecordMetadata> getNextPage(final CsvMemento memento, final Integer batchSize) {
        LOGGER.debug("Getting next page of records, up to {}", batchSize);

        final List<SpeechRecordMetadata> page = new ArrayList<>();

        while (page.size() < batchSize && memento.getCsvToBeanIterator().hasNext()) {
            final SpeechRecordMetadata record = memento.getCsvToBeanIterator().next();

            LOGGER.debug("Reading record: ", record);
            page.add(record);
        }

        return page;
    }

    protected void closeMemento(final CsvMemento memento) {
        try {
            memento.getCsvReader().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
