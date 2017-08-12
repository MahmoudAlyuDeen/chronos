package com.afterapps.chronos.api;

/*
 * Created by mahmoudalyudeen on 7/24/17.
 */

import com.afterapps.chronos.api.Responses.ReverseGeoLocResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReverseGeoLocService {

    String reverseGeoEndPoint = "maps/api/timezone/json";

    @GET(reverseGeoEndPoint)
    Call<ReverseGeoLocResponse> getReverseGeo(
            @Query("key") String apiKey,
            @Query("timestamp") String timestamp,
            @Query("location") String coordinates
    );
}
