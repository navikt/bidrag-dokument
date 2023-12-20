package no.nav.bidrag.dokument.security

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import java.net.URI
import java.util.Stack
import kotlin.collections.HashMap

class HttpHeaderTestRestTemplate(val testRestTemplate: TestRestTemplate) {

    private val headersForSingleCallbacks = Stack<Pair<String, String>>()
    private val valueGenerators: MutableMap<String, ValueGenerator> = HashMap()
    fun <T> exchange(url: String?, httpMethod: HttpMethod?, httpEntity: HttpEntity<*>, responseClass: Class<T>?): ResponseEntity<T> {
        return testRestTemplate.exchange(url, httpMethod, newEntityWithAddedHeaders(httpEntity), responseClass)
    }

    fun <T> exchange(
        url: String?,
        httpMethod: HttpMethod?,
        httpEntity: HttpEntity<*>?,
        typeReference: ParameterizedTypeReference<T>?,
    ): ResponseEntity<T> {
        return testRestTemplate.exchange(url, httpMethod, newEntityWithAddedHeaders(httpEntity), typeReference)
    }

    fun <T> postForEntity(url: String?, httpEntity: HttpEntity<*>, responseClass: Class<T>?): ResponseEntity<T> {
        return testRestTemplate.postForEntity(url, newEntityWithAddedHeaders(httpEntity), responseClass)
    }

    inline fun <reified T : Any> getForEntity(uri: URI, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.GET, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> putForEntity(uri: URI, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.PUT, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> postForEntity(uri: URI, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.POST, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> patchForEntity(uri: URI, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.PATCH, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> optionsForEntity(uri: URI, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.OPTIONS, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> delete(uri: URI): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.DELETE, newEntityWithAddedHeaders())
    }

    inline fun <reified T : Any> getForEntity(uri: String, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.GET, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> putForEntity(uri: String, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.PUT, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> postForEntity(uri: String, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.POST, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> patchForEntity(uri: String, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.PATCH, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> optionsForEntity(uri: String, request: Any? = null): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.OPTIONS, newEntityWithAddedHeaders(request))
    }

    inline fun <reified T : Any> delete(uri: String): ResponseEntity<T> {
        return testRestTemplate.exchange(uri, HttpMethod.DELETE, newEntityWithAddedHeaders())
    }

    fun newEntityWithAddedHeaders(request: Any? = null): HttpEntity<*> {
        val tempHeaders = HttpHeaders().apply {
            valueGenerators.forEach { add(it.key, it.value.generate()) }
        }

        while (!headersForSingleCallbacks.empty()) {
            val headerWithValue = headersForSingleCallbacks.pop()
            tempHeaders.add(headerWithValue.first, headerWithValue.second)
        }

        if (request is HttpEntity<*>) {
            return HttpEntity(request.body, tempHeaders.apply { putAll(request.headers) })
        }
        return HttpEntity(request, tempHeaders)
    }

    fun add(headerName: String, valueGenerator: ValueGenerator) {
        valueGenerators[headerName] = valueGenerator
    }

    fun addHeaderForSingleHttpEntityCallback(headerName: String, headerValue: String) {
        val headerWithValue = headerName to headerValue
        headersForSingleCallbacks.push(headerWithValue)
    }

    fun interface ValueGenerator {
        fun generate(): String?
    }
}
