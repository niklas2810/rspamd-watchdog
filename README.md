<div style="width:100%;padding:0px;margin:0px" align="center">
    <img src="https://socialify.git.ci/niklas2810/rspamd-watchdog/image?description=1&font=Rokkitt&language=1&owner=1&pattern=Brick%20Wall&theme=Dark" alt="aws-s3-watchdog" width="640" height="320" />
    <br>
    <a href="https://github.com/niklas2810/rspamd-watchdog/actions">
        <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/niklas2810/rspamd-watchdog/Build%20Main%20Branch%20&%20Docker%20Image?logo=github&style=for-the-badge">
    </a>
     <a href="https://hub.docker.com/r/niklas2810/rspamd-watchdog">
        <img alt="Docker Hub" src="https://img.shields.io/badge/Docker%20Hub-Link-blue?style=for-the-badge&logo=docker"/>
     </a>
    <br>
    <br>   
</div>

RSPAMD Watchdog interacts with Mailcow's API to notify the user about rejected/flagged email.
The application supports checking multiple instances at once.

This project is very similar to [AWS S3 Watchdog](https://github.com/niklas2810/aws-s3-watchdog).

Docker Hub Link: https://hub.docker.com/r/niklas2810/rspamd-watchdog

Dependencies:

- [healthchecksio-java](https://github.com/niklas2810/healthchecksio-java)
- [simple-java-mail](https://github.com/bbottema/simple-java-mail)
- [sentry-logback](https://github.com/getsentry/sentry-java)
- [logback-classic](http://logback.qos.ch)
- [dotenv](https://github.com/cdimascio/dotenv-java) 
- [lombok](https://github.com/rzwitserloot/lombok)