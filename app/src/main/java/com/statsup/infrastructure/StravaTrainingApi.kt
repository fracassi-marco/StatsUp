package com.statsup.infrastructure

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.statsup.domain.Athlete
import com.statsup.domain.Training
import com.statsup.domain.TrainingApi
import topinambur.Bearer
import topinambur.Http

class StravaTrainingApi : TrainingApi {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override suspend fun download(token: String, latest: Training?): List<Training> {
        val size = 100
        val allTrainings = mutableListOf<Training>()
        var page = 1
        while (true) {
            val params = mutableMapOf("page" to page.toString(), "per_page" to size.toString())
            if (latest != null) {
                params["after"] = latest.date.toEpochSecond().toString()
            }
            val response = Http().get(
                url = "https://www.strava.com/api/v3/athlete/activities",
                params = params,
                auth = Bearer(token),
                headers = mapOf("Accept" to "application/json")
            )
            val typeRef: TypeReference<List<Training>> = object : TypeReference<List<Training>>() {}
            val trainings = jsonMapper.readValue(response.body, typeRef)
            allTrainings.addAll(trainings)
            if (trainings.size < size) break
            page++
        }
        return allTrainings
    }

    override suspend fun athlete(token: String): Athlete {
        val response = Http().get(
            url = "https://www.strava.com/api/v3/athlete",
            auth = Bearer(token),
            headers = mapOf("Accept" to "application/json")
        )
        return jsonMapper.readValue(response.body)
    }

}
