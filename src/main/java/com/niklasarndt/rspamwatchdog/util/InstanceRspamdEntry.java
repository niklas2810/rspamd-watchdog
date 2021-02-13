package com.niklasarndt.rspamwatchdog.util;

import com.niklasarndt.rspamwatchdog.api.model.RspamdHistoryEntry;

public class InstanceRspamdEntry {

    private final String host;
    private final RspamdHistoryEntry entry;


    public InstanceRspamdEntry(String host, RspamdHistoryEntry entry) {
        this.host = host;
        this.entry = entry;
    }

    public String getHost() {
        return host;
    }

    public RspamdHistoryEntry getEntry() {
        return entry;
    }
}
