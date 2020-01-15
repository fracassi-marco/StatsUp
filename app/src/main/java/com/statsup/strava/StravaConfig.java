package com.statsup.strava;

import retrofit2.Retrofit;

public class StravaConfig extends Config {

    public StravaConfig(Retrofit retrofit) {
        super(retrofit);
    }

    public static StravaConfig.Builder withToken(String token) {
        return new StravaConfig.Builder(token);
    }

    public static StravaConfig.Builder withToken(Token token) {
        return withToken(token.toString());
    }

    public static class Builder {
        private static final String STRAVA_BASE_URL = "https://www.strava.com/api/v3/";

        private String token;
        private String baseURL = STRAVA_BASE_URL;
        private boolean debug = false;

        public Builder(String token) {
            this.token = token;
        }

        public StravaConfig build() {
            return new StravaConfig(createRetrofit(debug, baseURL, new AuthorizationInterceptor(token)));
        }
    }
}
