package com.statsup.strava;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthenticationRest {

    @POST("/oauth/token")
    @FormUrlEncoded
    Call<LoginResult> token(
            @Field("client_id") int clientID,
            @Field("client_secret") String clientSecret,
            @Field("code") String code);

    @POST("/oauth/deauthorize")
    Call<Void> deauthorize();
}
