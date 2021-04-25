package com.niklasarndt.rspamwatchdog.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niklasarndt.rspamwatchdog.api.model.RspamdHistoryEntry;
import com.niklasarndt.rspamwatchdog.util.BuildConstants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class LogFetcher {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final URL base;
    private final String url;
    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient.Builder().build();
    private final Logger logger;

    public LogFetcher(String baseUrl, String apiKey) throws MalformedURLException {
        this.base = new URL(baseUrl);
        if (!base.getProtocol().equals("http") && !base.getProtocol().equals("https"))
            throw new MalformedURLException("You are only allowed to use HTTP(s) connections.");

        this.logger = LoggerFactory.getLogger("LogFetcher (" + base.getHost() + ")");

        this.url = base.getProtocol() + "://" + base.getHost() + (base.getPort() != -1 ? ":" + base.getPort() : "")
                + base.getPath() + (base.getPath().endsWith("/") ? "" : "/")
                + "api/v1/get/logs/rspamd-history/";
        logger.info("Set base url to {}", url);
        this.apiKey = apiKey;
    }

    public boolean testConnection() {
        try {
            return getHistory(1) != null;
        } catch (Exception e) {
            logger.error("Connection test failed: {}", e.getMessage(), e);
            return false;
        }
    }


    public RspamdHistoryEntry[] getHistory(int maxEntries) {
        if (maxEntries < 1)
            throw new IllegalArgumentException("maxEntries must be >0 but is " + maxEntries);

        Request.Builder builder = new Request.Builder()
                .url(this.url + maxEntries)
                .header("User-Agent", "rspamd-watchdog:" + BuildConstants.VERSION)
                .header("Content-Type", "application/json")
                .header("X-Api-Key", apiKey);

        try {
            final Response response = client.newCall(builder.build()).execute();

            if (!response.isSuccessful()) {
                logger.error("Mailcow rejected the request: HTTP Code {} ({})",
                        response.code(), response.body() != null ?
                                response.body().string() : "No message");
                return new RspamdHistoryEntry[0];
            }

            if (response.body() == null) {
                logger.error("Mailcow didn't send a response!");
                return new RspamdHistoryEntry[0];
            }

            final String body = Objects.requireNonNull(response.body()).string();

            try {
                return MAPPER.readValue(body, RspamdHistoryEntry[].class);
            } catch (JsonProcessingException e) {
                logger.error("Could not parse response from Mailcow: {}", e.getMessage(), e);
                return new RspamdHistoryEntry[0];
            }

        } catch (IOException e) {
            logger.error("Failed to receive Mailcow response {}", e.getMessage(), e);
            return new RspamdHistoryEntry[0];
        }
    }

    public String getHost() {
        return base.getHost();
    }
}
