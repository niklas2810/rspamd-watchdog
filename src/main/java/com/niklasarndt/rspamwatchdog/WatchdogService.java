package com.niklasarndt.rspamwatchdog;

import com.niklasarndt.healthchecksio.Healthchecks;
import com.niklasarndt.healthchecksio.HealthchecksClient;
import com.niklasarndt.rspamwatchdog.api.LogFetcher;
import com.niklasarndt.rspamwatchdog.api.model.RspamdHistoryEntry;
import com.niklasarndt.rspamwatchdog.mail.MailService;
import com.niklasarndt.rspamwatchdog.util.BuildConstants;
import com.niklasarndt.rspamwatchdog.util.EnvHelper;
import com.niklasarndt.rspamwatchdog.util.InstanceRspamdEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WatchdogService implements Runnable {


    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMMMM dd yyyy HH:mm:ss z", Locale.US);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MailService mailer;
    private final HealthchecksClient health = EnvHelper.has("HEALTHCHECKS_ID") ?
            Healthchecks.forUuid(EnvHelper.require("HEALTHCHECKS_ID")) : null;
    private final List<LogFetcher> fetchers = new ArrayList<>();
    private final int historySize;
    private final String mailSubject = EnvHelper.require("MAIL_SUBJECT");
    private long lastUpdate = EnvHelper.DEBUG ? 0 : System.currentTimeMillis();

    WatchdogService(MailService mailer) {
        this.mailer = mailer;
        this.historySize = EnvHelper.requireInt("HISTORY_SIZE");

        if (health != null)
            logger.info("Sending pings to healthchecks.io!");
        else
            logger.info("Healthchecks.io integration is disabled.");

        if (historySize < 1) {
            logger.error("HISTORY_SIZE has to be >0!");
            System.exit(1);
        }

        setupFetchers();


        if (fetchers.size() == 0) {
            logger.error("No Mailcow Instances could be loaded, exiting.");
            System.exit(1);
        }
    }

    private void setupFetchers() {
        final String[] ids = EnvHelper.get("INSTANCE_IDS").orElseThrow(() -> new IllegalStateException("Please fill the INSTANCE_IDS environment variable.")).split(",");

        for (String id : ids) {
            id = id.trim();
            if (id.length() == 0)
                continue;
            String url = EnvHelper.get(id + "_URL", null);
            String key = EnvHelper.get(id + "_KEY", null);

            if (url == null || url.length() == 0) {
                logger.warn("Skipping {}: The URL for this Mailcow Instance was not found.", id);
                continue;
            }
            if (key == null || key.length() == 0) {
                logger.warn("Skipping {}: The API Key for this Mailcow Instance was not found.", id);
                continue;
            }

            try {
                final LogFetcher fetcher = new LogFetcher(url, key);
                if (!fetcher.testConnection()) {
                    logger.warn("Skipping {}: Connection test for this Mailcow Instance failed.", id);
                    continue;
                }
                fetchers.add(fetcher);
            } catch (MalformedURLException e) {
                logger.warn("Skipping {}: Could not parse URL - {} (URL was {})", id, e.getMessage(), url);
            }
        }
    }

    @Override
    public void run() {
        logger.debug("Checking the logs for spam emails..");

        if (health != null) {
            try {
                health.start().get();
            } catch (Exception e) {
                logger.error("Could not contact healthchecks.io", e);
            }
        }

        try {
            long start = System.currentTimeMillis();

            List<InstanceRspamdEntry> results = new ArrayList<>();

            for (LogFetcher fetcher : fetchers) {
                Arrays.stream(fetcher.getHistory(historySize)).filter(i -> i.after(lastUpdate) && !i.wasAccepted())
                        .map(i -> new InstanceRspamdEntry(fetcher.getHost(), i))
                        .collect(Collectors.toCollection(() -> results));
            }

            lastUpdate = start;

            if (results.size() == 0) {
                logger.debug("Found no rejected emails.");
                return;
            }

            mailer.send(mailSubject.replace("%amount%", results.size() + ""),
                    buildMail(results));

            if (health != null) {
                try {
                    health.success().get();
                } catch (Exception e) {
                    logger.error("Could not contact healthchecks.io", e);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error while fetching logs: {}", e.getMessage(), e);
            try {
                health.fail(e.getMessage()).get();
            } catch (Exception ex) {
                logger.error("Could not contact healthchecks.io", ex);
            }
        }


    }

    private String buildMail(List<InstanceRspamdEntry> results) {
        StringBuilder builder = new StringBuilder("<ul>\n");


        String currentHost = null;

        for (InstanceRspamdEntry result : results) {
            if (!result.getHost().equals(currentHost)) {
                if (currentHost != null)
                    builder.append("</ul>\n</li>\n");
                builder.append("<li>").append(result.getHost()).append("<ul>");
                currentHost = result.getHost();
            }

            final RspamdHistoryEntry entry = result.getEntry();

            final String text = String.format("<pre>%s<pre>(<b>%s</b> %.01f/%.01f, %s)",
                    entry.getSenderMime(), entry.getAction(),
                    entry.getScore(), entry.getRequiredScore(), DATE_FORMAT.format(entry.getDate()));
            builder.append("<li>").append(text).append("</li>\n");
        }

        builder.append("</ul>\n<br><p>This is an automated message from <b>")
                .append(BuildConstants.NAME)
                .append("</b> v.").append(BuildConstants.VERSION)
                .append(" (Build: ").append(BuildConstants.TIMESTAMP).append(").</p>");

        return builder.toString().trim();
    }
}
