# RSPAMD Watchdog

RSPAMD Watchdog interacts with Mailcow's API to notify the user about rejected/flagged email.
The application supports checking multiple instances at once.

This project is very similar to [AWS S3 Watchdog](https://github.com/niklas2810/aws-s3-watchdog).

Dependencies:

- [healthchecksio-java](https://github.com/niklas2810/healthchecksio-java)
- [simple-java-mail](https://github.com/bbottema/simple-java-mail)
- [sentry-logback](https://github.com/getsentry/sentry-java)
- [logback-classic](http://logback.qos.ch)
- [dotenv](https://github.com/cdimascio/dotenv-java) 
- [lombok](https://github.com/rzwitserloot/lombok)