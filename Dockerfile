FROM navikt/java:10
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

ADD ./bidrag-dokument-microservice/target/*.jar /.
WORKDIR /

ARG version
EXPOSE 8080

CMD ["java", "-jar", " bidrag-dokument-microservice-$version.jar"]
