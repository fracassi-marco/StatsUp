package com.statsup.domain

class StravaApiException(statusCode: Int) : Exception(
    when (statusCode) {
        401, 403 -> "Strava authentication error ($statusCode). Please try re-importing."
        429 -> "Strava rate limit exceeded. Try again in a few minutes."
        else -> "Strava API error (HTTP $statusCode). Try again later."
    }
)
