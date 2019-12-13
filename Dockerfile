FROM navikt/java:12
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

ADD ./target/bidrag-dokument-*.jar app.jar
COPY init-scripts /init-scripts

EXPOSE 8080
