package com.afterapps.chronos.api;

/*
 * Created by mahmoudalyudeen on 6/27/17.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    public static <S> S createService(Class<S> serviceClass, String apiBaseUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson));

        Retrofit retrofit = builder.build();
        return retrofit.create(serviceClass);
    }
}
