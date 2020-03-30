package com.statsup

import android.content.Context
import android.os.AsyncTask
import com.statsup.strava.*

class StravaActivities(
    private val context: Context,
    private val code: String,
    private val confs: Confs,
    private val onComplete: () -> Unit
) : AsyncTask<Void, Void, List<com.statsup.strava.Activity>>() {

    override fun doInBackground(vararg ignore: Void): List<com.statsup.strava.Activity> {
        val authenticationConfig = AuthenticationConfig.create().build()
        val api = AuthenticationAPI(authenticationConfig)
        val result = api.getTokenForApp(AppCredentials.with(confs.stravaClientId().toInt(), confs.stravaClientSecret()))
                .withCode(code)
                .execute()

        val config = StravaConfig.withToken(result.token)
                .build()
        val activityAPI = ActivityAPI(config)

        return download(activityAPI, 1)
    }

    private fun download(activityAPI: ActivityAPI, page: Int): MutableList<com.statsup.strava.Activity> {
        val itemsPerPage = 100
        val activities = activityAPI.listMyActivities()
                .inPage(page)
                .perPage(itemsPerPage)
                .execute()
        if (activities.size == itemsPerPage) {
            activities.addAll(download(activityAPI, page + 1))
        }
        return activities
    }

    override fun onPostExecute(activities: List<com.statsup.strava.Activity>) {
        ActivityRepository.clean(context)
        ActivityRepository.saveAll(context, activities.map { asRun(it) })
        onComplete.invoke()
    }

    private fun asRun(it: com.statsup.strava.Activity): Activity {
        var title = it.name
        if (title.isEmpty()) {
            title = it.type.name.capitalize()
        }
        return Activity(it.id, it.type, it.distance.meters, it.elapsedTime.seconds, it.startDateLocal.time, title, it.maxSpeed.metersPerSecond.toDouble())
    }
}
