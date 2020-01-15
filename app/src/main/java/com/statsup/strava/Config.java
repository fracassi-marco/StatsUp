package com.statsup.strava;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.statsup.Sports;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class Config {

    private Retrofit retrofit;

    public Config(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    protected static Retrofit createRetrofit(boolean debug, String baseURL, Interceptor... interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        for(Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }

        OkHttpClient client = builder.build();

        return new Retrofit.Builder().baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(makeGson()))
                .build();
    }

    private static Gson makeGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Distance.class, new DistanceTypeAdapter())
                .registerTypeAdapter(Time.class, new TimeTypeAdapter())
                .registerTypeAdapter(Coordinates.class, new CoordinatesTypeAdapter())
                .registerTypeAdapter(Sports.class, new SportsAdapter())
                .registerTypeAdapter(Speed.class, new SpeedTypeAdapter())
                .registerTypeAdapter(Token.class, new TokenTypeAdapter())
                .create();
    }
}
