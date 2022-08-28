package com.medallia.references.speechapi.publish;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The metadata for a voice recording.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString(onlyExplicitlyIncluded = true)
public class SpeechRecordMetadata {

    // The following are required attributes for the Medallia Speech API.

    @JsonProperty("call_identifier")
    @CsvBindByName(column = "call_identifier", required = true)
    @ToString.Include
    private String callIdentifier;

    @JsonProperty("speech_file_name")
    @CsvBindByName(column = "speech_file_name", required = true)
    @ToString.Include
    private String speechFileName;

    @JsonProperty("unit_identifier")
    @CsvBindByName(column = "unit_identifier", required = true)
    @ToString.Include
    private String unitIdentifier;

    @JsonProperty("call_date_and_time")
    @CsvBindByName(column = "call_date_and_time", required = true)
    @ToString.Include
    private String callDateAndTime;

    // The following are optional attributes for the Medallia Speech API.

    @JsonProperty("call_recording_url")
    @CsvCustomBindByName(column = "call_recording_url", converter = EmptyStringToNullStringConverter.class)
    private String callRecordingUrl;

    @JsonProperty("vertical_model")
    @CsvCustomBindByName(column = "vertical_model", converter = StringToVerticalModelConverter.class)
    private VerticalModel verticalModel;

    @JsonProperty("locale")
    @CsvCustomBindByName(column = "locale", converter = EmptyStringToNullStringConverter.class)
    private String locale;

    @JsonProperty("agent_locale")
    @CsvCustomBindByName(column = "agent_locale", converter = EmptyStringToNullStringConverter.class)
    private String agentLocale;

    @JsonProperty("apply_diarization")
    @CsvCustomBindByName(column = "apply_diarization", converter = EmptyStringToNullStringConverter.class)
    private String applyDiarization;

    @JsonProperty("agent_channel")
    @CsvCustomBindByName(column = "agent_channel", converter = StringToAgentChannelInRecordingConverter.class)
    private AgentChannelInRecording agentChannel;

    @JsonProperty("substitutions")
    @CsvCustomBindByName(column = "substitutions", converter = StringToMapConverter.class)
    private Map<String, String> substitutions;

    @JsonProperty("apply_redaction")
    @CsvCustomBindByName(column = "apply_redaction", converter = StringToBooleanConverter.class)
    private BooleanString applyRedaction;

    @JsonProperty("first_name")
    @CsvCustomBindByName(column = "first_name", converter = EmptyStringToNullStringConverter.class)
    private String firstName;

    @JsonProperty("last_name")
    @CsvCustomBindByName(column = "last_name", converter = EmptyStringToNullStringConverter.class)
    private String lastName;

    @JsonProperty("email")
    @CsvCustomBindByName(column = "email", converter = EmptyStringToNullStringConverter.class)
    private String email;

    @JsonProperty("phone_number")
    @CsvCustomBindByName(column = "phone_number", converter = EmptyStringToNullStringConverter.class)
    private String phoneNumber;

    @JsonProperty("connection_id")
    @CsvCustomBindByName(column = "connection_id", converter = EmptyStringToNullStringConverter.class)
    @Deprecated
    private String connectionId;

    @JsonProperty("profile_uuid")
    @CsvCustomBindByName(column = "profile_uuid", converter = EmptyStringToNullStringConverter.class)
    @Deprecated
    private String profileUuid;

    @JsonProperty("engine")
    @CsvCustomBindByName(column = "engine", converter = StringToEngineConverter.class)
    private Engine engine;

    @JsonProperty("connector_id")
    @CsvCustomBindByName(column = "connector_id", converter = EmptyStringToNullStringConverter.class)
    private String connectorId;

    // Note that is more appropriate as Map<String, Object>, but for the sake
    // of simplicity, we're just showing the Map<String, String> subcase.
    @JsonProperty("speech_additional_info")
    @CsvCustomBindByName(column = "speech_additional_info", converter = StringToMapConverter.class)
    private Map<String, String> speechAdditionalInfo;

    public enum VerticalModel {
        CALL_CENTER("Call Center"),
        FINANCIAL_SERVICES("Financial Services"),
        HEALTHCARE("Healthcare"),
        VOICEMAIL("Voicemail"),
        SURVEY("Survey"),
        LARGE_VOCAB("Large Vocab"),
        GENERAL("General"),
        ;

        private String displayName;

        VerticalModel(final String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return this.displayName;
        }
    }

    public enum AgentChannelInRecording {
        AGENT_0_CLIENT_1("0"),
        AGENT_1_CLIENT_0("1"),
        ;

        private String displayName;

        AgentChannelInRecording(final String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return this.displayName;
        }
    }

    public enum BooleanString {
        YES("Yes"),
        NO("No"),
        ;

        private String displayName;

        BooleanString(final String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return this.displayName;
        }
    }

    public enum Engine {
        ENGINE_1("Engine 1"),
        ENGINE_2("Engine 2"),
        ENGINE_3("Engine 3"),
        ;

        private String displayName;

        Engine(final String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return this.displayName;
        }
    }

}
