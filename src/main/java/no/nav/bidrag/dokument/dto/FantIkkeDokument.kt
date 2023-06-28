package no.nav.bidrag.transport.dokument

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

fun fantIkkeDokument(melding: String): Nothing { throw HttpClientErrorException(HttpStatus.NOT_FOUND, melding) }
