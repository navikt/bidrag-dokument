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
