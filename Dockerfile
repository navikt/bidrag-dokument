FROM ghcr.io/navikt/baseimages/temurin:17
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

COPY ./target/app-exec.jar app.jar
COPY init-scripts /init-scripts
ENV SPRING_PROFILES_ACTIVE=nais
EXPOSE 8080
