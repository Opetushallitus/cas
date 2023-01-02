# CAS-virkailija

This project is based on [cas-overlay-template (branch 6.4)](https://github.com/apereo/cas-overlay-template/tree/6.3).

## Requirements

Java 11

Project includes gradle wrapper so it doesn't have to be installed. Just use `./gradlew` (unix) and `gradlew.bat` (win).

## Build

    gradle build

## Database

    docker-compose --file docker-compose.dependencies.yml up

## Run

    java -jar build/libs/cas.war

## Configuration

Default configuration is in [application.yml](src/main/resources/application.yml).
To extend or override default configuration add system variables to run command:

    -Dserver.port=8081

or create yml file

    server.port: 8081

... and add following to run command:

    -Dcas.standalone.configurationFile=<path_to_file>

To utilize other OPH services from test environment, add following to run command:

    -DbaseUrl=https://<domain_to_test_environment>
    -Dserviceprovider.app.username.to.usermanagement=<username_to_test_environment>
    -Dserviceprovider.app.password.to.usermanagement=<password_to_test_environment>

Note that `baseUrl` parameter cannot be used with yml file!
(See [java-utils/java-properties](https://github.com/Opetushallitus/java-utils/tree/master/java-properties)).
