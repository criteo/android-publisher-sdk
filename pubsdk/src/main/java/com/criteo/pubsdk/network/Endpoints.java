package com.criteo.pubsdk.network;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Endpoints {

    @POST("cdb")
    Call<JsonObject> cdb(@Query("profileId") String profile, @Body JsonObject body);
}
