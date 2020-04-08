package com.statsup

enum class Sports(val id: Long, val code: String, val title: Int, val icon: Int) {
    All(0, "All", R.string.sports_all, R.drawable.workout),
    RUN(1, "Run", R.string.sports_run, R.drawable.run),
    WALK(2, "Walk", R.string.sports_walk, R.drawable.walk),
    WORKOUT(3, "Workout", R.string.sports_workout, R.drawable.workout),
    ALPINE_SKI(4, "AlpineSki", R.string.sports_alpine_ski,R.drawable.alpine_ski),
    BACKCOUNTRY_SKI(5, "BackcountrySki", R.string.sports_backcountry_ski, R.drawable.backcountry_ski),
    CANOEING(6, "Canoeing", R.string.sports_canoeing, R.drawable.canoeing),
    CROSSFIT(7, "Crossfit", R.string.sports_crossfit, R.drawable.crossfit),
    EBIKE_RIDE(8, "EBikeRide", R.string.sports_ebike_ride, R.drawable.ebike_ride),
    ELLIPTICAL(9, "Elliptical", R.string.sports_elliptical, R.drawable.elliptical),
    HAND_CYCLE(10, "HandCycle", R.string.sports_hand_cycle, R.drawable.hand_cycle),
    RIDE(11, "Ride", R.string.sports_ride, R.drawable.ride),
    HIKE(12, "Hike", R.string.sports_hike, R.drawable.hike),
    ICE_SKATE(13, "IceSkate", R.string.sports_ice_skate, R.drawable.ice_skate),
    INLINE_SKATE(14, "InlineSkate", R.string.sports_inline_skate, R.drawable.inline_skate),
    KAYAK(15, "Kayaking", R.string.sports_kayak, R.drawable.kayak),
    KITESURF(16, "Kitesurf", R.string.sports_kitesurf, R.drawable.kitesurf),
    NORDIC_SKI(17, "NordicSki", R.string.sports_nordic_ski, R.drawable.nordic_ski),
    CLIMBING(18, "RockClimbing", R.string.sports_climbing, R.drawable.climbing),
    ROLLER_SKI(19, "RollerSki", R.string.sports_roller_ski, R.drawable.roller_ski),
    ROWING(20, "Rowing", R.string.sports_rowing, R.drawable.rowing),
    SNOWBOARD(21, "Snowboard", R.string.sports_snowboard, R.drawable.snowboard),
    SNOWSHOE(22, "Snowshoe", R.string.sports_snowshoe, R.drawable.snowshoe),
    STEPPER(23, "StairStepper", R.string.sports_stepper, R.drawable.stepper),
    PADDLEBOARD(24, "StandUpPaddling", R.string.sports_paddleboard, R.drawable.paddleboard),
    SURF(25, "Surfing", R.string.sports_surf, R.drawable.surf),
    SWIM(26, "Swim", R.string.sports_swim, R.drawable.swim),
    VIRTUAL_RIDE(27, "VirtualRide", R.string.sports_virtual_ride, R.drawable.cyclette),
    TREADMILL(28, "VirtualRun", R.string.sports_treadmill, R.drawable.treadmill),
    WEIGHT_TRAININIG(29, "WeightTraining", R.string.sports_weight_training, R.drawable.weight_training),
    WHEELCHAIR(30, "Wheelchair", R.string.sports_wheelchair, R.drawable.wheelchair),
    WINDSURF(31, "Windsurf", R.string.sports_windsurf, R.drawable.windsurf),
    YOGA(32, "Yoga", R.string.sports_yoga, R.drawable.yoga),
    GOLF(33, "Golf", R.string.sports_golf, R.drawable.golf),
    SAIL(34, "Sail", R.string.sports_sail, R.drawable.sail),
    SKATEBOARD(35, "Skateboard", R.string.sports_skateboard, R.drawable.skateboard),
    SOCCER(36, "Soccer", R.string.sports_soccer, R.drawable.soccer),
    VELOMOBILE(37, "Velomobile", R.string.sports_velomobile, R.drawable.velomobile);

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