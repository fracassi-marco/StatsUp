package com.statsup.strava;

import retrofit2.Call;

public class DeauthorizationRequest {

    private final AuthenticationRest restService;
    private final AuthenticationAPI api;

    public DeauthorizationRequest(AuthenticationRest restService, AuthenticationAPI api) {
        this.restService = restService;
        this.api = api;
    }

    public Void execute() {
        Call<Void> call = restService.deauthorize();
        return api.execute(call);
    }
}
