FROM navikt/java:10
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

ADD ./target/bidrag-dokument-*.jar app.jar

EXPOSE 8080
