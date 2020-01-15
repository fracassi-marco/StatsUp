package com.statsup.strava;

import com.google.gson.annotations.SerializedName;

public class LoginResult {
    @SerializedName("access_token")
    private Token token;

    public Token getToken() {
        return token;
    }
}
