package com.medallia.references.speechapi.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Results from the publish attempt for a single record.  This is only
 * used when a PARTIALLY_ACCEPTED response on the larger request is
 * returned.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SpeechPublishTaskDetails {

    @JsonProperty("call_identifier")
    private String callIdentifier;

    @JsonProperty("speech_file_name")
    private String speechFileName;

    @JsonProperty("status")
    private TaskStatus status;

    @JsonProperty("error_message")
    private String errorMessage;

    public enum TaskStatus {
        ACCEPTED("ACCEPTED"),
        REJECTED("REJECTED"),
        ;

        private String displayName;

        TaskStatus(final String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return this.displayName;
        }
    }

}
