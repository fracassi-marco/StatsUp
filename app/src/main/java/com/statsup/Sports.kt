package com.statsup

enum class Sports(val id: Long, val title: Int, val icon: Int) {

    RUN(1, R.string.run_title_default, R.drawable.run),
    WALK(2, R.string.walk_title_default, R.drawable.walk),
    WORKOUT(3, R.string.workout_title_default, R.drawable.workout),
    ALPINE_SKI(4, R.string.alpine_ski_title_default,R.drawable.alpine_ski),
    BACKCOUNTRY_SKI(5, R.string.backcountry_ski, R.drawable.backcountry_ski),
    CANOEING(6, R.string.canoeing, R.drawable.canoeing),
    CROSSFIT(7, R.string.crossfit, R.drawable.crossfit),
    EBIKE_RIDE(8, R.string.ebike_ride, R.drawable.ebike_ride),
    ELLIPTICAL(9, R.string.elliptical, R.drawable.elliptical),
    HAND_CYCLE(10, R.string.hand_cycle, R.drawable.hand_cycle),
    RIDE(11, R.string.ride, R.drawable.ride),
    HIKE(12, R.string.hike, R.drawable.hike),
    ICE_SKATE(13, R.string.ice_skate, R.drawable.ice_skate),
    INLINE_SKATE(14, R.string.inline_skate, R.drawable.inline_skate),
    KAYAK(15, R.string.kayak, R.drawable.kayak),
    KITESURF(16, R.string.kitesurf, R.drawable.kitesurf),
    NORDIC_SKI(17, R.string.nordic_ski, R.drawable.nordic_ski),
    CLIMBING(18, R.string.climbing, R.drawable.climbing),
    ROLLER_SKI(19, R.string.roller_ski, R.drawable.roller_ski),
    ROWING(20, R.string.rowing, R.drawable.rowing),
    SNOWBOARD(21, R.string.snowboard, R.drawable.snowboard),
    SNOWSHOE(22, R.string.snowshoe, R.drawable.snowshoe),
    STEPPER(23, R.string.stepper, R.drawable.stepper),
    PADDLEBOARD(24, R.string.paddleboard, R.drawable.paddleboard),
    SURF(25, R.string.surf, R.drawable.surf),
    SWIM(26, R.string.swim, R.drawable.swim),
    CYCLETTE(27, R.string.cyclette, R.drawable.cyclette),
    TREADMILL(28, R.string.treadmill, R.drawable.treadmill),
    WEIGHT_TRAININIG(29, R.string.weight_training, R.drawable.weight_training),
    WHEELCHAIR(30, R.string.wheelchair, R.drawable.wheelchair),
    WINDSURF(31, R.string.windsurf, R.drawable.windsurf),
    YOGA(32, R.string.yoga, R.drawable.yoga),
    MEDITATION(33, R.string.meditation, R.drawable.meditation);

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
    }
}