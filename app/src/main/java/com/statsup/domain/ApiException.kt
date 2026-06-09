package com.statsup.domain

class ApiException(val statusCode: Int) : Exception(
    when (statusCode) {
        401, 403 -> "Authentication error ($statusCode). Please try re-importing."
        429 -> "Rate limit exceeded. Try again in a few minutes."
        else -> "API error (HTTP $statusCode). Try again later."
    }
) {
    val isAuthError: Boolean get() = statusCode == 401 || statusCode == 403
}
