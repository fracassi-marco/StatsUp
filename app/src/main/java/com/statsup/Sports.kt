package com.statsup

enum class Sports(val id: Long, val code: String, val title: Int, val icon: Int) {

    RUN(1, "Run", R.string.run_title_default, R.drawable.run),
    WALK(2, "Walk", R.string.walk_title_default, R.drawable.walk),
    WORKOUT(3, "Workout", R.string.workout_title_default, R.drawable.workout),
    ALPINE_SKI(4, "AlpineSki", R.string.alpine_ski_title_default,R.drawable.alpine_ski),
    BACKCOUNTRY_SKI(5, "BackcountrySki", R.string.backcountry_ski, R.drawable.backcountry_ski),
    CANOEING(6, "Canoeing", R.string.canoeing, R.drawable.canoeing),
    CROSSFIT(7, "Crossfit", R.string.crossfit, R.drawable.crossfit),
    EBIKE_RIDE(8, "EBikeRide", R.string.ebike_ride, R.drawable.ebike_ride),
    ELLIPTICAL(9, "Elliptical", R.string.elliptical, R.drawable.elliptical),
    HAND_CYCLE(10, "HandCycle", R.string.hand_cycle, R.drawable.hand_cycle),
    RIDE(11, "Ride", R.string.ride, R.drawable.ride),
    HIKE(12, "Hike", R.string.hike, R.drawable.hike),
    ICE_SKATE(13, "IceSkate", R.string.ice_skate, R.drawable.ice_skate),
    INLINE_SKATE(14, "InlineSkate", R.string.inline_skate, R.drawable.inline_skate),
    KAYAK(15, "Kayaking", R.string.kayak, R.drawable.kayak),
    KITESURF(16, "Kitesurf", R.string.kitesurf, R.drawable.kitesurf),
    NORDIC_SKI(17, "NordicSki", R.string.nordic_ski, R.drawable.nordic_ski),
    CLIMBING(18, "RockClimbing", R.string.climbing, R.drawable.climbing),
    ROLLER_SKI(19, "RollerSki", R.string.roller_ski, R.drawable.roller_ski),
    ROWING(20, "Rowing", R.string.rowing, R.drawable.rowing),
    SNOWBOARD(21, "Snowboard", R.string.snowboard, R.drawable.snowboard),
    SNOWSHOE(22, "Snowshoe", R.string.snowshoe, R.drawable.snowshoe),
    STEPPER(23, "StairStepper", R.string.stepper, R.drawable.stepper),
    PADDLEBOARD(24, "StandUpPaddling", R.string.paddleboard, R.drawable.paddleboard),
    SURF(25, "Surfing", R.string.surf, R.drawable.surf),
    SWIM(26, "Swim", R.string.swim, R.drawable.swim),
    CYCLETTE(27, "VirtualRide", R.string.cyclette, R.drawable.cyclette),
    TREADMILL(28, "VirtualRun", R.string.treadmill, R.drawable.treadmill),
    WEIGHT_TRAININIG(29, "WeightTraining", R.string.weight_training, R.drawable.weight_training),
    WHEELCHAIR(30, "", R.string.wheelchair, R.drawable.wheelchair),
    WINDSURF(31, "Windsurf", R.string.windsurf, R.drawable.windsurf),
    YOGA(32, "Yoga", R.string.yoga, R.drawable.yoga),
    MEDITATION(33, "Meditation", R.string.meditation, R.drawable.meditation);

    companion object {
        @JvmStatic
        fun icon(sport: String): Int {
            return valueOf(sport.toUpperCase()).icon
        }

        @JvmStatic
        fun title(sport: String): Int {
            return valueOf(sport.toUpperCase()).title
        }

        @JvmStatic
        fun byId(id: Long): Sports {
            return values().asList().single { it.id == id }
        }

        @JvmStatic
        fun byCode(code: String): Sports {
            return values().asList().single { it.code.equals(code, true) }
        }
    }
}