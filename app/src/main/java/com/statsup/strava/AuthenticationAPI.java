package com.statsup.strava;

public class AuthenticationAPI extends StravaAPI{

    public AuthenticationAPI(Config config) {
        super(config);
    }

    public AuthenticationRequest getTokenForApp(AppCredentials appCredentials) {
        return new AuthenticationRequest(appCredentials, getAPI(AuthenticationRest.class), this);
    }

    public DeauthorizationRequest deauthorize() {
        return new DeauthorizationRequest(getAPI(AuthenticationRest.class), this);
    }

}
