package com.statsup.strava;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ActivityRest {
    @GET("athlete/activities")
    Call<List<Activity>> getMyActivities(@Query("page") Integer page, @Query("per_page") Integer perPage);
}
