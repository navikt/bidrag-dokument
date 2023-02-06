# bidrag-dokument

![](https://github.com/navikt/bidrag-dokument/workflows/continuous%20integration/badge.svg)
![](https://github.com/navikt/bidrag-dokument/workflows/test%20build%20on%20pull%20request/badge.svg)
[![release bidrag-dokument](https://github.com/navikt/bidrag-dokument/actions/workflows/release.yaml/badge.svg)](https://github.com/navikt/bidrag-dokument/actions/workflows/release.yaml)

Microservice for integrasjon mellom BISYS og JOARK, bruk av `bidrag-dokument-journalpost`
og `bidrag-dokument-arkiv`.

### hensikt

`bidrag-dokument` vil tilby et REST grensesnitt som brukes av BISYS for å bruke
tjenester tilbudt av JOARK men som må tilpasses BISYS sin domenemodell. Denne tjenesten
kaller andre mikrotjenester og blir kontrollert av `bidrag-dokument-ui`. Andre
tjenester som blir "rutet" via denne:

* `bidrag-dokument-arkiv`
* `bidrag-dokument-journalpost`

### bygg og kjør applikasjon

Dette er en spring-boot applikasjon og kan kjøres som ren java applikasjon, ved å
bruke `maven` eller ved å bygge et docker-image og kjøre dette

Se avsnittet `Sikkerhet` for kjøring med sikkerhet lokalt.

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

### Profiler

Applikasjonen er satt opp med følgende profiler:

#### nais-profil

Formål: Kjøring i produksjon og dev-cluster.

#### test-profil

Formål: Enhetstester generelt, og for lokal kjøring.

#### secure-test-profil

Formål: Brukes av JournalpostControllerTest for å skyte inn test-token i TestRestTemplate.

### Sikkerhet

Tjenestens endepunkter er sikret med navikt
[token-validation-spring](https://github.com/navikt/token-support/tree/master/token-validation-spring)
fra [token-support](https://github.com/navikt/token-support). Det betyr at gyldig
OIDC-id-token må være inkludert som Bearer-token i Authorization header for alle
spørringer mot disse endepunktene.

For kjøring lokalt benyttes
[token-validation-test-support](https://github.com/navikt/token-support/tree/master/token-validation-test-support)
som blant annet sørger for at det genereres id-tokens til test formål. For å redusere
risikoen for at testgeneratoren ved en feil gjøres aktiv i produksjon er
token-validation-test-support-modulen kun tilgjengelig i test-scope. I tillegg er bruken av
testgeneratoren kun knyttet til en egen spring-boot app-definisjon,
BidragDokumentLocal (lokalisert under test) som benytter test-profil.

BidragDokumentLocal brukes i stedet for BidragDokument ved lokal kjøring.

AUD bidrag-q-localhost er lagt til for å støtte localhost redirect i preprod. Denne benyttes ved
front-end-utvikling for å kunne kjøre tester med
preprod-tjenester uten å måtte legge inn host-mappinger. bidrag-q-localhost-agenten er satt opp
vha https://github.com/navikt/amag. Denne er ikke,
og skal heller ikke være tilgjengelig i prod.

#### Oppskrift for kjøring med test-token i Swagger

(ved integrasjonstesting mot AM eller ABAC må token hentes fra bidrag-ui.<domene-navn>/session)

- Start BidragDokumentLocal som standard Java-applikasjon
- Hent
  test-token [http://localhost:8090/bidrag-dokument/local/jwt](http://localhost:8090/bidrag-dokument/local/jwt)
- Åpne Swagger (http://localhost:8090/bidrag-dokument/swagger-ui.html)
- Trykk Authorize, og oppdater value-feltet med: Bearer <testtoken-streng> fra steg 2.

### Lokal kjøring mot nais

For å kunne kjøre lokalt mot sky må du gjøre følgende

Åpne terminal på root mappen til `bidrag-dokument`
Konfigurer kubectl til å gå mot kluster `dev-fss`

```bash
# Sett cluster til dev-fss
kubectx dev-fss
# Sett namespace til bidrag
kubens bidrag 

# -- Eller hvis du ikke har kubectx/kubens installert 
# (da må -n=bidrag legges til etter exec i neste kommando)
kubectl config use dev-fss
```

Deretter kjør følgende kommando for å importere secrets. Viktig at filen som opprettes ikke
committes til git

```bash
kubectl exec --tty deployment/bidrag-dokument-feature printenv | grep -E 'AZURE_|TOKEN_X|_URL|SCOPE|CLIENT_ID' > src/test/resources/application-lokal-nais-secrets.properties
```

Deretter kan tokenet brukes til å logge inn på
swagger-ui http://localhost:8080/bidrag-dokument/swagger-ui/index.html og teste ut ulike api kall
