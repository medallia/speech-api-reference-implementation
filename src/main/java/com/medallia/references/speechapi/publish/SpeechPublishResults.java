package com.medallia.references.speechapi.publish;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The response from the Medallia Speech API related to publishing a set
 * of metadata for processing.  For those records which are accepted, an
 * asynchronous job has been created; if successful, results will appear
 * in the Medallia Experience Cloud instance in a few moments.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString
public class SpeechPublishResults {

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("status")
    private JobStatus jobStatus;

    @JsonProperty("details")
    private List<SpeechPublishTaskDetails> details;

    public enum JobStatus {
        ACCEPTED("ACCEPTED"),
        PARTIALLY_ACCEPTED("PARTIALLY_ACCEPTED"),
        REJECTED("REJECTED"),
        ;

        private String displayName;

        JobStatus(final String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return this.displayName;
        }
    }

}
