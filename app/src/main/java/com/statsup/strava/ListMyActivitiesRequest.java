package com.statsup.strava;

import java.util.List;

import retrofit2.Call;

public class ListMyActivitiesRequest {

    private final ActivityRest restService;
    private final StravaAPI api;
    private Integer page;
    private Integer perPage;

    public ListMyActivitiesRequest(ActivityRest restService, StravaAPI api) {
        this.api = api;
        this.restService = restService;
    }

    public ListMyActivitiesRequest inPage(int page) {
        this.page = page;
        return this;
    }

    public ListMyActivitiesRequest perPage(int perPage) {
        this.perPage = perPage;
        return this;
    }

    public List<Activity> execute() {
        Call<List<Activity>> call = restService.getMyActivities(page, perPage);
        return api.execute(call);
    }

}
