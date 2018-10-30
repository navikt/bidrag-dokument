Feature: bidrag-dokument REST API

    Tester REST API til journalpost endepunktet i bidrag-dokument.
    URLer til tjenester hentes via fasit.adeo.no og gjøres ved å spesifisere
    alias til en RestService record i fasit for et gitt miljø.

    Background: Spesifiser base-url til tjenesten her så vi slipper å gjenta for hvert scenario.
                Fasit environment er gitt ved environment variabler ved oppstart av container.
        Given restservice 'bidragDokument'

    Scenario: Sjekk at vi får en liste med journalposter
        When jeg henter journalpost "0000003"
        Then statuskoden skal være '200'
        And resultatet skal være en liste med journalposter
        And hver journalpost i listen skal ha saksnummer '0000003' i 'saksnummerBidrag' feltet

    Scenario: Sjekk innholdet av en enkelt journalpost i bidrag
        When jeg henter journalpost "0000003"
        Then statuskoden skal være '200'
        And resultatet skal være en liste med journalposter
        And hver rad i listen skal ha følgende properties satt:
            | fagomrade          |
            | dokumenter         |
            | saksnummerBidrag   |

    Scenario: Sjekk at ukjent sak gir 204 med ingen data
        When jeg henter journalpost "XYZ"
        Then skal resultatet ikke være et journalpost objekt
        And statuskoden skal være '204'

