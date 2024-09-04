FROM amazoncorretto:21 AS build
WORKDIR /app

COPY gradle ./gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .

COPY lombok.config .
COPY etc ./etc
COPY src ./src
RUN ./gradlew clean build -x test

FROM amazoncorretto:21

WORKDIR /app
COPY services ./services
COPY config ./config
COPY --from=build /app/build/libs/cas.war .

CMD [ "java", "-Dcas.standalone.configurationFile=/app/config/${ENV}.yml", "-jar", "cas.war" ]
