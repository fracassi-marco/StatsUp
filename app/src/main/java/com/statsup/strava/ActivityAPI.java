package com.statsup.strava;


public class ActivityAPI extends StravaAPI {

    public ActivityAPI(StravaConfig config) {
        super(config);
    }

    public ListMyActivitiesRequest listMyActivities() {
        return new ListMyActivitiesRequest(getAPI(ActivityRest.class), this);
    }
}
