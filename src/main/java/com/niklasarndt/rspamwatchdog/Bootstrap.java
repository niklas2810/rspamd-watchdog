package com.niklasarndt.rspamwatchdog;

import com.niklasarndt.rspamwatchdog.util.BuildConstants;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        LOG.info("This is {} v.{} (Build {})",
                BuildConstants.NAME,
                BuildConstants.VERSION, BuildConstants.TIMESTAMP);

        final Scheduler scheduler = new Scheduler();

        activateSentryLogging();

        scheduler.start();
    }

    private static void activateSentryLogging() {
        if (System.getenv("SENTRY_DSN") != null || System.getProperty("sentry.dsn") != null)
            Sentry.init();
        else
            LOG.warn("Sentry is not initialized. Logging will be local only.");


        if (Sentry.isEnabled()) {
            Sentry.configureScope(scope -> {
                scope.setExtra("App Name", BuildConstants.NAME);
                scope.setExtra("App Version", BuildConstants.VERSION);
                scope.setExtra("App Build Time", BuildConstants.TIMESTAMP);
                scope.setExtra("Java Vendor", System.getProperty("java.vendor"));
                scope.setExtra("Java Version", System.getProperty("java.vm.version"));
                scope.setExtra("Operating System", System.getProperty("os.name"));
            });
            LOG.info("Sentry logging is active now!");
        }
    }
}
