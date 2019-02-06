# bidrag-dokument
Microservice for integrasjon mellom BISYS og JOARK

### hensikt

`bidrag-dokument` vil tilby et REST grensesnitt som brukes av BISYS for å bruke
tjenester tilbudt av JOARK men som må tilpasses BISYS sin domenemodell. Denne tjenesten
kaller andre mikrotjenester og blir kontrollert av `bidrag-dokument-ui`. Andre 
tjenester som blir "rutet" via denne:
* `bidrag-dokument-journalpost`
* `bidrag-dokument-arkiv`

### bygg og kjør applikasjon

Dette er en spring-boot applikasjon og kan kjøres som ren java applikasjon, ved å
bruke `maven` eller ved å bygge et docker-image og kjøre dette 

Se [Sikkerhet](#Sikkerhet) for kjøring med sikkerhet lokalt.

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
`http://localhost:8080/bidrag-dokument/swagger-ui.html`

### Sikkerhet
Tjenestens endepunkter er sikret med navikt [oidc-spring-support](https://github.com/navikt/token-support/tree/master/oidc-spring-support)
fra [token-support](https://github.com/navikt/token-support). Det betyr at gyldig OIDC-id-token må være inkludert som Bearer-token i Authorization 
header for alle spørringer mot disse endepunktene. 

For kjøring lokalt benyttes [oidc-test-support](https://github.com/navikt/token-support/tree/master/oidc-test-support) som blant annet sørger for
at det genereres id-tokens til test formål. For å redusere risikoen for at testgeneratoren ved en feil gjøres aktiv i produksjon er 
oidc-test-support-modulen kun tilgjengelig i test-scope. I tillegg er bruken av testgeneratoren kun knyttet til en egen spring-boot app-definisjon
, BidragDokumentLocal (lokalisert under test) som benytter dev-profil.

BidragDokumentLocal brukes i stedet for BidragDokument ved lokal kjøring.

#### Oppskrift for kjøring med sikkerhet lokalt
 - Start BidragDokumentLocal som standard Java-applikasjon
 
 - Opprette cookie for nettleser med token app-instans for bruk av oidc-test-support, naviger til:<br> 
 	 - [http://localhost:8080/bidrag-dokument/local/cookie?redirect=/bidrag-dokument](http://localhost:8080/bidrag-dokument/local/cookie?redirect=/bidrag-dokument)
 	 - Cookie med testtoken er nå tilgjengelig i nettleser, naviger til http://localhost:8080/bidrag-dokument/swagger-ui.html for å teste.
 	 
 - (Valgfri) Verifiser at test-tokengeneratoren fungerer ved å hente frem:<br>
 	 - [http://localhost:8080/bidrag-dokument/local/jwt](http://localhost:8080/bidrag-dokument/local/jwt)<br> 	  	
 	 - [http://localhost:8080/bidrag-dokument/local/cookie](http://localhost:8080/bidrag-dokument/local/cookie)<br> 	  	 
  	 - [http://localhost:8080/bidrag-dokument/local/claims](http://localhost:8080/bidrag-dokument/local/claims)<br>
  
#### Swagger Authorize 
Den grønne authorize-knappen øverst i Swagger-ui kan brukes til å autentisere requester om du har tilgang på et gyldig OIDC-token. For å benytte authorize må følgende legges i value-feltet:
 - "Bearer id-token" (hvor id-token er en gyldig jwt-tekst-streng)
 
 For localhost kan et gyldig id-token hentes med følgende URL dersom BidragDokumentArkivLocal er startet på port 8080:
   - [http://localhost:8080/bidrag-dokument/local/jwt](http://localhost:8080/bidrag-dokument/local/jwt)<br>
   
For preprod kan følgende CURL-kommando benyttes (krever tilgang til isso-agent-passord i Fasit for aktuelt miljø):
 
 <code> curl -X POST \ </code><br>
	  <code> -u "{isso-agent-brukernavn}:{isso-agent-passord}" \ </code> <br>
	  <code> -d "grant_type=client_credentials&scope=openid" \ </code> <br>
	  <code> {isso-issuer-url}/access_token </code> <br>
 
  
hvor <code>{isso-agent-brukernavn}</code> og <code>{isso-agent-passord}</code> hentes fra Fasit-ressurs OpenIdConnect bidrag-dokument-ui-oidc for aktuelt miljø (f.eks [https://fasit.adeo.no/resources/6419841](https://fasit.adeo.no/resources/6419841) for q0),

og <code>{isso-issuer-url}</code> hentes fra Fasit-ressurs BaseUrl isso-issuer (f.eks [https://fasit.adeo.no/resources/2291405](https://fasit.adeo.no/resources/2291405) for q0.
 
 
