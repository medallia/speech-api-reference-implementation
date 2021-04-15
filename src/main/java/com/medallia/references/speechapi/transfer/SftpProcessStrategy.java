package com.medallia.references.speechapi.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The implementation of the transfer process as it relates to SFTP sources.
 */
@Component
public class SftpProcessStrategy extends GenericProcessStrategy {

    private final Map<Long, Map<SourceOptions, SftpInstance>> threadConnectionCache = new HashMap<>();

    @Autowired
    public SftpProcessStrategy(final MmftService mmftService) {
        super(mmftService);
    }

    protected Collection<String> getFilenames(final SourceOptions options) {
        final SftpInstance sftp = getSftpConnection(options);

        return sftp.list(
            options.getSftp().getFolder(),
            (directoryName) -> false, // no recursion
            (filename) -> true        // accept everything found
        );
    }

    protected byte[] getBytesFromSource(
        final String filename,
        final SourceOptions options
    ) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final SftpInstance sftp = getSftpConnection(options);

            sftp.download(
                filename,
                byteArrayOutputStream
            );

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to download remote file", e);
        }
    }

    private SftpInstance getSftpConnection(final SourceOptions options) {
        final Long threadId = Thread.currentThread().getId();

        if (!threadConnectionCache.containsKey(threadId)) {
            threadConnectionCache.put(threadId, new HashMap<>(1));
        }

        final Map<SourceOptions, SftpInstance> connectionCache = threadConnectionCache.get(threadId);

        if (!connectionCache.containsKey(options)) {
            final SftpInstance sftpInstance = new SftpInstance(options.getSftp());
            sftpInstance.connect();

            connectionCache.put(
                options,
                sftpInstance
            );
        }

        return connectionCache.get(options);
    }

}
