package com.afterapps.chronos.api;

/*
 * Created by mahmoudalyudeen on 6/27/17.
 */

import com.afterapps.chronos.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    private static <S> S createService(Class<S> serviceClass, String apiBaseUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson));

        Retrofit retrofit = builder.build();
        return retrofit.create(serviceClass);
    }

    public static ReverseGeoLocService createGeoLocService() {
        return createService(ReverseGeoLocService.class, Constants.REVERSE_GEO_LOC_API_BASE_UEL);
    }

    public static TimingsService createTimingsService() {
        return createService(TimingsService.class, Constants.PRAYER_TIMINGS_API_BASE_URL);
    }
}
