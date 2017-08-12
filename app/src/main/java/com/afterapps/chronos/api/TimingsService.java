package com.afterapps.chronos.api;

/*
 * Created by mahmoudalyudeen on 7/24/17.
 */

import com.afterapps.chronos.api.Responses.TimingsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TimingsService {

    String timingsEndpoint = "calendar";

    @GET(timingsEndpoint)
    Call<TimingsResponse> getTimings(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("timezonestring") String timezoneId,
            @Query("month") int month,
            @Query("year") int year,
            @Query("method") int method,
            @Query("school") int school,
            @Query("latitudeAdjustmentMethod") int latitudeMethod
    );
}
