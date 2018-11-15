# bidrag-dokument
Microservice for integrasjon mellom BISYS og JOARK

### hensikt

`bidrag-dokument` vil tilby et REST grensesnitt som brukes av BISYS for å bruke
tjenester tilbudt av JOARK men som må tilpasses BISYS sin domenemodell. Denne tjenesten
kaller andre mikrotjenester og blir kontrollert av `bidrag-dokument-ui`. Andre 
tjenester som blit "rutet" via denne:
* `bidrag-dokument-journalpost`
* `bidrag-dokument-arkivk`

### bygg og kjør applikasjon

Dette er en spring-boot applikasjon og kan kjøres som ren java applikasjon, ved å
bruke `maven` eller ved å bygge et docker-image og kjøre dette 

##### java og maven
* krever installasjon av java og maven

`mvn clean install`<br>
deretter<br>
`mvn spring-boot:run`<br>
eller<br>
`cd target`<br>
`java -jar bidrag-dokument-<versjon>.jar`

##### docker og maven
* krever installasjon av java, maven og docker
* docker image er det som blir kjørt som nais applikasjon

`mvn clean install`<br>
deretter<br>
`docker build -t bidrag-dokument .`<br>
`docker run -p 8080:8080 bidrag-dokument`

Etter applikasjon er startet kan den nåes med browser på
`http://localhost:8080/bidrag-dokument/v2/api-docs`
