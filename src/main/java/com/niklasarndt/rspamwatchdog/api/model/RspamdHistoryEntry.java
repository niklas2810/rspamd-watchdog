package com.niklasarndt.rspamwatchdog.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.Date;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RspamdHistoryEntry {

    @JsonProperty("unix_time")
    private String unixTime;
    private String action;

    @JsonProperty("sender_mime")
    private String senderMime;

    @JsonProperty("required_score")
    private double requiredScore;

    private String ip;

    private String subject;

    private double score;

    private double size;


    public Date getDate() {
        if (unixTime == null)
            return null;

        return new Date(unixMilliseconds());
    }

    public long unixMilliseconds() {
        return Long.parseLong(unixTime) * 1000;
    }

    public boolean after(long unixMilliseconds) {
        return unixMilliseconds() > unixMilliseconds;
    }

    public boolean wasAccepted() {
        return action == null || action.equals("no action");
    }
}
