package com.criteo.pubsdk.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Endpoints {
    @POST("inapp/v1")
    Call<JsonObject> cdb(@Body JsonObject body);

    @GET("v1.0/api/config")
    Call<JsonObject> config(@Query("networkId") int networkId,
                            @Query("appId") String appId,
                            @Query("sdkVersion") String sdkVersion);
}
