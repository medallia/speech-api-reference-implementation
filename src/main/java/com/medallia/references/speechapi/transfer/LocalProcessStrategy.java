package com.medallia.references.speechapi.transfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The implementation of the transfer process as it relates to local sources.
 */
@Component
public class LocalProcessStrategy extends GenericProcessStrategy {

    @Autowired
    public LocalProcessStrategy(final MmftService mmftService) {
        super(mmftService);
    }

    protected Collection<String> getFilenames(final SourceOptions source) {
        try (Stream<Path> stream = Files.list(source.getLocal().getFolder())) {
            return stream
                .filter(file -> !Files.isDirectory(file))
                .filter(file -> Files.isReadable(file))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Unable to list local filenames", e);
        }
    }

    protected byte[] getBytesFromSource(
        final String filename,
        final SourceOptions source
    ) {
        try {
            return Files.readAllBytes(source.getLocal().getFolder().resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Unable to get contents of file", e);
        }
    }

}
