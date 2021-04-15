package com.medallia.references.speechapi.transfer;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import lombok.extern.slf4j.Slf4j;

/**
 * Wraps the <a href="http://www.jcraft.com/jsch/">Jsch library</a> for
 * handling SFTP transfers.
 */
@Slf4j
public class SftpInstance {

    public static final Integer CONNECT_TIMEOUT_MSEC = 30000;
    public static final List<String> SPECIAL_IGNORE = ImmutableList.of(".", "..");

    private Session session = null;
    private ChannelSftp channel = null;

    private final SftpOptions options;

    public SftpInstance(final SftpOptions options) {
        this.options = options;
    }

    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    public void connect() {
        if (isConnected()) {
            LOGGER.debug("Already connected, reusing existing connection");
            return;
        }

        if (channel != null || session != null) {
            LOGGER.debug("Forcing reconnection");
            disconnect();
        }

        try {
            final JSch jsch = new JSch();

            LOGGER.debug("Starting session");

            session = jsch.getSession(
                options.getUsername(),
                options.getHost(),
                options.getPort()
            );

            final Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.setPassword(options.getPassword());

            LOGGER.debug("Connecting session");

            session.connect(CONNECT_TIMEOUT_MSEC);

            LOGGER.debug("Starting channel");

            channel = (ChannelSftp) session.openChannel("sftp");

            LOGGER.debug("Connecting channel");

            channel.connect();

            LOGGER.debug("SFTP session initialized");
        } catch (JSchException e) {
            throw new IllegalStateException("Could not connect to SFTP server", e);
        }
    }

    private void disconnect() {
        if (channel != null && channel.isConnected()) {
            LOGGER.debug("Disconnecting channel");
            channel.disconnect();
        }

        if (session != null && session.isConnected()) {
            LOGGER.debug("Disconnecting session");
            session.disconnect();
        }

        channel = null;
        session = null;
    }

    public List<String> list(
            final String remoteDir,
            final Function<String, Boolean> visitChildDirectory,
            final Function<String, Boolean> isFileInteresting
    ) {
        connect();

        try {
            LOGGER.debug("Listing SFTP folder {}", remoteDir);

            final Deque<String> pathsToVisit = new ArrayDeque<>();
            pathsToVisit.add(normalizeDirectory(remoteDir));

            final List<String> files = new ArrayList<>();

            while (pathsToVisit.size() > 0) {
                final String path = pathsToVisit.pop();

                channel.ls(
                    path,
                    new ChannelSftp.LsEntrySelector() {
                        @Override
                        public int select(final ChannelSftp.LsEntry entry) {
                            final SftpATTRS attributes = entry.getAttrs();
                            final String filename = entry.getFilename();
                            final String fullPath = normalizePath(path, filename);

                            if (!SPECIAL_IGNORE.contains(filename)) {
                                if (attributes.isDir()) {
                                    if (visitChildDirectory.apply(fullPath)) {
                                        pathsToVisit.add(normalizeDirectory(fullPath));
                                    }
                                } else {
                                    if (isFileInteresting.apply(filename)) {
                                        files.add(fullPath);
                                    }
                                }
                            }

                            return ChannelSftp.LsEntrySelector.CONTINUE;
                        }
                    }
                );
            }

            LOGGER.debug("Found {} file(s)", files.size());

            return files;
        } catch (SftpException e) {
            throw new IllegalStateException("Unable to list remote directory", e);
        }
    }

    public void download(
            final String fullPath,
            final OutputStream outputStream
    ) {
        connect();

        try {
            LOGGER.debug("Downloading {}", fullPath);

            channel.get(
                fullPath,
                outputStream,
                new SftpProgressMonitor() {
                    @Override
                    public boolean count(final long count) {
                        /* Progress update */
                        LOGGER.debug("Received {} byte(s)", count);
                        return true;
                    }

                    @Override
                    public void end() {
                        /* Finished transferring */
                        LOGGER.debug("Finished transfer");
                    }

                    @Override
                    public void init(final int op, final String src, final String dest, final long max) {
                        /* Started data transfer */
                        LOGGER.debug("Started transfer");
                    }
                }
            );
        } catch (SftpException e) {
            throw new IllegalStateException(
                String.format("Unable to download remote file: %s", fullPath),
                e
            );
        }
    }

    private static String normalizeDirectory(final String dir) {
        return String.format(
            "%s%s",
            dir,
            dir.endsWith("/") ? "" : "/"
        );
    }

    private static String normalizePath(final String dir, final String file) {
        return String.format(
            "%s%s",
            normalizeDirectory(dir),
            file
        );
    }

}
