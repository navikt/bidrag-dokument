FROM navikt/java:10
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

ARG version
COPY bidrag-dokument-microservice/target/bidrag-dokument-microservice-$version.jar app.jar

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]
