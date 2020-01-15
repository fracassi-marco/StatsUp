package com.statsup.strava;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public abstract class StravaAPI {
    private static final int UNAUTHORIZED_CODE = 401;
    private final Config config;

    public StravaAPI(Config config) {
        this.config = config;
    }

    protected <T> T getAPI(Class<T> apiRest) {
        return config.getRetrofit().create(apiRest);
    }

    public <T> T execute(Call<T> call) {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new RuntimeException("A network error happened contacting Strava API", e);
        }

        if (response.isSuccessful()) {
            return response.body();
        } else if (response.code() == UNAUTHORIZED_CODE) {
            throw new RuntimeException();
        } else {
            throw new RuntimeException("Response was not successful");
        }
    }
}
