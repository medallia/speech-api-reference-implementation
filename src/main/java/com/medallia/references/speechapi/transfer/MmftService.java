package com.medallia.references.speechapi.transfer;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Wraps the <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html">AWS SDK for Java</a>
 * with the customizations needed for Medallia Media File Transfer (MMFT).
 */
@Component
@Slf4j
public class MmftService {

    @Autowired
    private RetryTemplate retryTemplate;

    /**
     * Uploads the {@code data} payload provided to MMFT using the
     * {@code filename} provided as part of the S3 key.
     * @param filename the filename portion of the S3 key
     * @param data the payload to upload
     * @param options the MMFT-related options
     */
    public void upload(
            final String filename,
            final byte[] data,
            final MmftOptions options
    ) {
        try {
            final S3Client s3 = getS3(options);

            final String key = getKey(options.getFolder(), filename);

            LOGGER.debug("Uploading {}", key);

            retryTemplate.execute((context) -> {
                // Since audio recordings can be very large (100+ MB each),
                // it is best to use a multipart upload.  With the AWS SDK
                // v1, we would use a TransferManager.  However, that
                // has not yet been ported over to the AWS SDK v2.  As such,
                // we use a putObject() call below just to show how this
                // would work in concept.  Production systems should
                // follow the multipart upload strategy.
                //
                // Github ticket tracking adding TransferManager to v2:
                // https://github.com/aws/aws-sdk-java-v2/issues/37

                return s3.putObject(
                    PutObjectRequest.builder()
                        .bucket(options.getBucket())
                        .key(key)
                        .build(),
                    RequestBody.fromBytes(data)
                );
            });
        } catch (S3Exception e) {
            throw new RuntimeException("Failed uploading file", e);
        }
    }

    /**
     * Creates an S3 client with the MMFT customizations applied.
     * @param options the MMFT-related options
     * @return the S3 client
     */
    private static S3Client getS3(final MmftOptions options) {
        try {
            return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(options.getAccessKey(), options.getSecretKey())
                ))
                .endpointOverride(new URI(options.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
                )
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the directory/filename pair into an S3-compatible key.
     * @param directory the directory
     * @param filename the filename
     * @return the S3 key
     */
    private String getKey(final String directory, final String filename) {
        return StringUtils.removeEnd(
            StringUtils.removeStart(
                String.format(
                    "%s/%s",
                    StringUtils.removeEnd(StringUtils.removeStart(directory, "/"), "/"),
                    StringUtils.removeEnd(StringUtils.removeStart(filename, "/"), "/")
                ),
                "/"
            ),
            "/"
        );
    }
}
