package com.statsup

import android.content.Context
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import kotlinx.coroutines.CoroutineScope
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import topinambur.async.httpAsync

private const val STRAVA = "https://www.strava.com/api/v3"

class StravaActivities(
    private val context: Context,
    private val code: String,
    private val scope: CoroutineScope,
    private val onComplete: () -> Unit
) {
    suspend fun download() {
        val token = token()
        val activities = download(1, token)
        onPostExecute(activities)
    }

    private suspend fun token() : String{
        val config = Confs(context)
        val response = "$STRAVA/oauth/token".httpAsync(scope).post(
                data = mapOf(
                    "client_id" to config.stravaClientId,
                    "client_secret" to config.stravaClientSecret,
                    "code" to code
                )
            ).await()

        return Json.parse(response.body).asObject().get("access_token").asString()
    }

    private suspend fun download(page: Int, token: String) : List<Activity> {
        val response = "$STRAVA/athlete/activities".httpAsync(scope).get(
            params = mapOf("page" to page.toString(), "per_page" to "100"),
            headers = mapOf("Accept" to "application/json", "Authorization" to "Bearer $token")
        ).await()
        val activities = Json.parse(response.body).asArray().map {
            val item = it.asObject()
            val map = item.get("map").asObject().get("summary_polyline")
            Activity(
                item.get("id").asLong(),
                Sports.byCode(item.get("type").asString()),
                item.get("distance").asFloat(),
                item.get("elapsed_time").asInt(),
                item.get("start_date").asDate().millis,
                item.get("name").asString(),
                item.get("max_speed").asDouble(),
                item.get("total_elevation_gain").asDouble(),
                if(map.isNull) null else map.asString()
            )
        }

        if (activities.size == 100) {
            return activities.plus(download(page + 1, token))
        }

        return activities
    }

    private fun onPostExecute(activities: List<Activity>) {
        ActivityRepository.clean(context)
        ActivityRepository.saveAll(context, activities)
        onComplete.invoke()
    }
}

private fun JsonValue.asDate(): DateTime {
    return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC().parseDateTime(
        this.asString()
    ).withZone(DateTimeZone.getDefault())
}
