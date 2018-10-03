# bidrag-dokument
Microservice for integrasjon mellom BISYS og JOARK

### arkitektur

Tjenesten består av 2 elementer:
* `bidrag-dokument-model`
  * en samling av jar-filer som legges sammen med applikasjonen når den kjøres runtime
* `bidrag-dokument-microservice`
  * en applikasjon som tilbyr integrasjon mellom BISYS og JOARK - microservice 

### hensikt

`bidrag-dokument-microservice` vil tilby et REST grensesnitt som brukes av BISYS for å
bruke tjenester tilbudt av JOARK men som må tilpasses BISYS sin domenemodell. For å 
tilpasse til `bisys` brukes `bidrag-dokument-model` som består av:
* `bidrag-dokument-consumer` som er konsumenten av tjenestene til JOARK
* `bidrag-dokument-service` som er tjenestene som blir direkte kallet av
`bidrag-dokument-microservice`
* `bidrag-dokument-domain` er modellen. Den inneholder domener, dtoer og domenelogikk
til å knytte sammen JOARK og BISYS domenene. 

### bygg og kjør applikasjon

Dette er en spring-boot applikasjon og kan kjøres som ren java applikasjon, ved å
bruke `maven` eller ved å bygge et docker-image og kjøre dette 

##### java og maven
* krever installasjon av java og maven

`mvn clean install`<br>
deretter<br>
`cd bidrag-dokument-microservice`<br>
`mvn spring-boot:run`<br>
eller<br>
`cd bidrag-dokument-microservice/target`<br>
`java -jar bidrag-dokument-microservice-<versjon>.jar`

##### docker og maven
* krever installasjon av java, maven og docker
* docker image er det som blir kjørt som nais applikasjon

`mvn clean install`<br>
deretter<br>
`docker build -t bidrag-dokument .`<br>
`docker run -p 8080:8080 bidrag-dokument`

Etter applikasjon er startet kan den nåes med browser på
`http://localhost:8080/bidrag-dokument/v2/api-docs`
