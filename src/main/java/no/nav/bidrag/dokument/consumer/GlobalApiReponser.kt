package no.nav.bidrag.dokument.consumer

import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses


@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ApiResponses(
    value = [
    ApiResponse(
        responseCode = "401",
        description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig",
    ), ApiResponse(
        responseCode = "403",
        description = "Saksbehandler har ikke tilgang til aktuell journalpost",
    )]
)
annotation class GlobalApiReponses