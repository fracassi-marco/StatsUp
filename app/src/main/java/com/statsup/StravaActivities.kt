package com.statsup

import android.os.AsyncTask
import com.sweetzpot.stravazpot.activity.api.ActivityAPI
import com.sweetzpot.stravazpot.activity.model.Activity
import com.sweetzpot.stravazpot.authenticaton.api.AuthenticationAPI
import com.sweetzpot.stravazpot.authenticaton.model.AppCredentials
import com.sweetzpot.stravazpot.common.api.AuthenticationConfig
import com.sweetzpot.stravazpot.common.api.StravaConfig


class StravaActivities(
    private val code: String,
    private val confs: Confs,
    private val onComplete: () -> Unit
) : AsyncTask<Void, Void, List<Activity>>() {

    override fun doInBackground(vararg ignore: Void): List<Activity> {
        val authenticationConfig = AuthenticationConfig.create()
                .debug()
                .build()
        val api = AuthenticationAPI(authenticationConfig)
        val result = api.getTokenForApp(AppCredentials.with(confs.stravaClientId(), confs.stravaClientSecret()))
                .withCode(code)
                .execute()

        val config = StravaConfig.withToken(result.token)
                .debug()
                .build()
        val activityAPI = ActivityAPI(config)

        return download(activityAPI, 1)
    }

    private fun download(activityAPI: ActivityAPI, page: Int): MutableList<Activity> {
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

    override fun onPostExecute(activities: List<Activity>) {
        ActivityRepository.addIfNotExists(activities.map { asRun(it) })
        onComplete.invoke()
    }

    private fun asRun(it: Activity): com.statsup.Activity {
        val sport = mapSport(it.type.name)
        return Activity(sport, it.distance.meters, it.elapsedTime.seconds, it.startDateLocal.time)
    }

    private fun mapSport(sport: String): Sports {
        return when (sport) {
            "ALPINESKI" -> Sports.ALPINE_SKI
            "BACKCOUNTRYSKI" -> Sports.BACKCOUNTRY_SKI
            "CANOEING" -> Sports.CANOEING
            "CROSSFIT" -> Sports.CROSSFIT
            "EBIKERIDE" -> Sports.EBIKE_RIDE
            "ELLIPTICAL" -> Sports.ELLIPTICAL
            "HANDCYCLE" -> Sports.HAND_CYCLE
            "HIKE" -> Sports.HIKE
            "ICESKATE" -> Sports.ICE_SKATE
            "INLINESKATE" -> Sports.INLINE_SKATE
            "KAYAKING" -> Sports.KAYAK
            "KITESURF" -> Sports.KITESURF
            "NORDICSKI" -> Sports.NORDIC_SKI
            "RIDE" -> Sports.RIDE
            "ROCKCLIMBING" -> Sports.CLIMBING
            "ROLLERSKI" -> Sports.ROLLER_SKI
            "ROWING" -> Sports.ROWING
            "RUN" -> Sports.RUN
            "SNOWBOARD" -> Sports.SNOWBOARD
            "SNOWSHOE" -> Sports.SNOWSHOE
            "STAIRSTEPPER" -> Sports.STEPPER
            "STANDUPPADDLING" -> Sports.PADDLEBOARD
            "SURFING" -> Sports.SURF
            "SWIM" -> Sports.SWIM
            "VIRTUALRIDE" -> Sports.CYCLETTE
            "VIRTUALRUN" -> Sports.TREADMILL
            "WALK" -> Sports.WALK
            "WEIGHTTRAINING" -> Sports.WEIGHT_TRAININIG
            "WHEELCHAIR" -> Sports.WHEELCHAIR
            "WINDSURF" -> Sports.WINDSURF
            "YOGA" -> Sports.YOGA
            else -> Sports.WORKOUT
        }
    }
}
