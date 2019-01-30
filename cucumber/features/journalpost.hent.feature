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

    Scenario: Sjekk at vi får en journalpost beriket med informasjon for den journalposten gjelder
        When jeg henter journalpost med id "BID-32350499"
        Then statuskoden skal være '200'
        And journalposten skal inneholde metadata om gjelderBrukerIds bidragssaker som inkluderer
            | roller     |
            | eierfogd   |
            | saksnummer |
            | saksstatus |

    Scenario: Sjekk at vi får BAD_REQUEST når vi ikke kildebestemmer hvor vi skal hente journalposten fra
        When jeg henter journalpost med id "101"
        Then statuskoden skal være '400'
