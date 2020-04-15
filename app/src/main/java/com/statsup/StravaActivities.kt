package com.statsup

import android.content.Context
import android.os.AsyncTask
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import topinambur.http

private const val STRAVA = "https://www.strava.com/api/v3"

class StravaActivities(
    private val context: Context,
    private val code: String,
    private val onComplete: () -> Unit
) : AsyncTask<Void, Void, List<Activity>>() {

    override fun doInBackground(vararg ignore: Void): List<Activity> {
        return download(1, token())
    }

    private fun token(): String {
        val config = Confs(context)
        val response = "$STRAVA/oauth/token"
            .http.post(
                data = mapOf(
                    "client_id" to config.stravaClientId(),
                    "client_secret" to config.stravaClientSecret(),
                    "code" to code
                )
            )

        return Json.parse(response.body).asObject().get("access_token").asString()
    }

    private fun download(
        page: Int,
        token: String
    ): List<Activity> {
        val response = "$STRAVA/athlete/activities".http.get(
            params = mapOf("page" to page.toString(), "per_page" to "100"),
            headers = mapOf("Accept" to "application/json", "Authorization" to "Bearer $token")
        )

        val activities = Json.parse(response.body).asArray().map {
            val item = it.asObject()
            Activity(
                item.get("id").asLong(),
                Sports.byCode(item.get("type").asString()),
                item.get("distance").asFloat(),
                item.get("elapsed_time").asInt(),
                item.get("start_date").asDate().millis,
                item.get("name").asString(),
                item.get("max_speed").asDouble(),
                item.get("total_elevation_gain").asDouble()
            )
        }

        if (activities.size == 100) {
            return activities.plus(download(page + 1, token))
        }
        return activities
    }

    override fun onPostExecute(activities: List<Activity>) {
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
