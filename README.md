#Judgels Uriel

[![Build Status](https://travis-ci.org/ia-toki/judgels-uriel.svg?branch=master)](https://travis-ci.org/ia-toki/judgels-uriel)

##Description
Uriel is an application built using [Play Framework](https://www.playframework.com/) to provide programming competition functions and services.

Currently Uriel only supports IOI style competition.

Uriel depends on [Sandalphon](https://github.com/ia-toki/judgels-sandalphon) to get programming problems, [Sealtiel](https://github.com/ia-toki/judgels-sealtiel) to send grading request to Gabriel and [Jophiel](https://github.com/ia-toki/judgels-jophiel) for authentication and authorization.

##Set Up And Run
To set up Sealtiel, you need to:

1. Clone [Judgels Play Commons](https://github.com/ia-toki/judgels-play-commons), [Gabriel Commons](https://github.com/ia-toki/judgels-gabriel-commons), and [Judgels Frontend Commons](https://github.com/ia-toki/judgels-frontend-commons) into the same level of Sandalphon directory, so that the directory looks like:
    - Parent Directory
        - gabriel-commons
        - judgels-frontend-commons
        - judgels-play-commons
        - judgels-uriel

2. Copy conf/application_default.conf into conf/application.conf and change the configuration accordingly. **Refer to the default configuration file for explanation of the configuration keys.** In the application configuration, Uriel need to connect to running Sandalphon (to render programming problems), Jophiel (for authentication and authorization) and Sealtiel (to grade programming problems) application. In order to connect Uriel to running Sandalphon, Jophiel and Sealtiel, Uriel must be registered as Sandalphon, Jophiel and Sealtiel clients.

3. Copy conf/db_default.conf into conf/db.conf and change the configuration accordingly. **Refer to the default configuration file for explanation of the configuration keys.**

To run Uriel, just run "activator" then it will check and download all dependencies and enter Play Console.
In Play Console use "run" command to run Sandalphon. By default it will listen on port 9000. For more information of Play Console, please read the [documentation](https://www.playframework.com/documentation/2.3.x/PlayConsole).

After login on Uriel using user on Jophiel, add "admin" value to role column of your user record on table "uriel\_user\_role" then relogin (logout and login again) to access full feature.

The version that is recommended for public use is [v0.1.0](https://github.com/ia-toki/judgels-uriel/tree/v0.1.0).
