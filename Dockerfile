FROM navikt/java:10
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

ADD ./bidrag-dokument-microservice/target/bidrag-dokument-microservice-*.jar app.jar

EXPOSE 8080
