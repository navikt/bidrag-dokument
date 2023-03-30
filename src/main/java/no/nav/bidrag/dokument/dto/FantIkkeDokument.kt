package no.nav.bidrag.dokument.dto

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

fun fantIkkeDokument(melding: String): Nothing { throw HttpClientErrorException(HttpStatus.NOT_FOUND, melding) }
