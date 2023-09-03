package no.nav.bidrag.dokument.service

import kotlinx.coroutines.ThreadContextElement
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class SecurityCoroutineContext(
    private val securityContext: SecurityContext = SecurityContextHolder.getContext(),
    private val requestContext: RequestAttributes? = RequestContextHolder.getRequestAttributes()
) : ThreadContextElement<SecurityContext?> {

    companion object Key : CoroutineContext.Key<SecurityCoroutineContext>

    override val key: CoroutineContext.Key<SecurityCoroutineContext> get() = Key

    override fun updateThreadContext(context: CoroutineContext): SecurityContext? {
        val previousSecurityContext = SecurityContextHolder.getContext()
        SecurityContextHolder.setContext(securityContext)
        RequestContextHolder.setRequestAttributes(requestContext)
        return previousSecurityContext.takeIf { it.authentication != null }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: SecurityContext?) {
        if (oldState == null) {
            SecurityContextHolder.clearContext()
        } else {
            SecurityContextHolder.setContext(oldState)
        }
    }
}

private fun getServletRequestAttributes(): ServletRequestAttributes? =
    RequestContextHolder.getRequestAttributes()
        ?.let {
            if (ServletRequestAttributes::class.java.isAssignableFrom(it.javaClass)) {
                it as ServletRequestAttributes
            } else {
                null
            }
        }

class RequestContextAsyncContext(
    private val contextMap: Map<String, String> = MDC.getCopyOfContextMap() ?: emptyMap(),
    private val servletRequestAttributes: ServletRequestAttributes? = getServletRequestAttributes(),
) : ThreadContextElement<Map<String, String>>, AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<RequestContextAsyncContext>


    override fun updateThreadContext(context: CoroutineContext): Map<String, String> {
        val oldState = MDC.getCopyOfContextMap() ?: emptyMap()

        MDC.setContextMap(contextMap)

        servletRequestAttributes?.let {
            RequestContextHolder.setRequestAttributes(it)
        }
        return oldState
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Map<String, String>) {
        RequestContextHolder.setRequestAttributes(null)
        MDC.setContextMap(oldState)
    }
}