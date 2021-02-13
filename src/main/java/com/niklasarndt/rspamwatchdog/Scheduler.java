package com.niklasarndt.rspamwatchdog;

import com.niklasarndt.rspamwatchdog.mail.MailService;
import com.niklasarndt.rspamwatchdog.util.BuildConstants;
import com.niklasarndt.rspamwatchdog.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MailService mailer = new MailService();
    private final WatchdogService watchdogService = new WatchdogService(mailer);

    private ScheduledExecutorService executor;

    public void start() {
        if (mailer.isInvalid()) {
            logger.error("Email configuration is invalid, aborting startup");
            return;
        }

        if (executor != null && !executor.isShutdown()) {
            logger.info("Shutting down existing executor...");
            executor.shutdown();
        }

        executor = Executors.newScheduledThreadPool(1);
        int interval = EnvHelper.requireInt("FETCH_INTERVAL");
        if (interval <= 0) {
            logger.error("The polling interval must be larger than 0!");
            return;
        }
        logger.info("Setting polling interval to {} seconds (every {} minutes)", interval,
                interval / 60);

        boolean initialDelay = EnvHelper.get("INITIAL_DELAY", "true")
                .equalsIgnoreCase("true");

        int delay = !initialDelay ? 0 : calculateStartDelay(interval);
        executor.scheduleAtFixedRate(watchdogService, delay, interval, TimeUnit.SECONDS);

    }

    private int calculateStartDelay(int secondInterval) {
        ZoneOffset offset = OffsetDateTime.now().getOffset
                ();
        long start = LocalDate.now().atStartOfDay().toInstant(offset).toEpochMilli();
        long iter = secondInterval * 1000;
        while (start < System.currentTimeMillis())
            start += iter;

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start),
                ZoneId.systemDefault());
        int res = (int) ((start - System.currentTimeMillis()) / 1000);

        logger.info("Starting {} at {} (in {} seconds)!",
                BuildConstants.NAME, startTime.toString(), res);
        return res;
    }
}
