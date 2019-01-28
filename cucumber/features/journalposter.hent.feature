Feature: bidrag-dokument REST API

    Tester REST API til journalpost endepunktet i bidrag-dokument.
    URLer til tjenester hentes via fasit.adeo.no og gjøres ved å spesifisere
    alias til en RestService record i fasit for et gitt miljø.

    Background: Spesifiser base-url til tjenesten her så vi slipper å gjenta for hvert scenario.
                Fasit environment er gitt ved environment variabler ved oppstart.
        Given restservice 'bidragDokument'

    Scenario: Sjekk at health endpoint er operativt
        When jeg kaller status endpoint
        Then skal tjenesten returnere 'OK'
        And statuskoden skal være '200'

    Scenario: Sjekk at vi får en liste med journalposter på fagområdet
        When jeg henter journalposter for sak "0000003" på fagområdet "BID"
        Then statuskoden skal være '200'
        And skal resultatet være en liste med journalposter
        And hver journalpost i listen skal ha 'saksnummer' '0000003'
        And hver journalpost i listen skal ha 'fagomrade' 'BID'

    Scenario: Sjekk innholdet av en enkelt journalpost i bidrag
        When jeg henter journalposter for sak "0000003" på fagområdet "BID"
        Then statuskoden skal være '200'
        And skal resultatet være en liste med journalposter
        And hver rad i listen skal ha følgende properties satt:
            | fagomrade   |
            | dokumenter  |
            | saksnummer  |

    Scenario: Sjekk at ukjent sak gir 204 med ingen data
        When jeg henter journalposter for sak "XYZ" på fagområdet "BID"
        Then statuskoden skal være '204'
