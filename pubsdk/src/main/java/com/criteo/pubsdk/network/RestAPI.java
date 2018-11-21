package com.criteo.pubsdk.network;

import com.criteo.pubsdk.BuildConfig;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public final class RestAPI {
    private static final String CDB_BASE_URL = "https://directbidder-test-app.par.preprod.crto.in";

    private RestAPI() {
    }

    public static JsonObject callCdb(String profile, User user, Publisher publisher, ArrayList<Slot> slots) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(getOkHttp())
                .build();
        Endpoints endpoints = retrofit.create(Endpoints.class);
        JsonObject body = new JsonObject();
        body.add("publisher", publisher.toJson());
        body.add("user", user.toJson());
        JsonArray slotsArray = new JsonArray();
        for (Slot slot : slots) {
            slotsArray.add(slot.toJson());
        }
        body.add("slots", slotsArray);
        Call<JsonObject> responseCall
                = endpoints.cdb(profile, body);
        Response<JsonObject> retrofitResponse = null;
        JsonObject result = new JsonObject();
        try {
            retrofitResponse = responseCall.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (retrofitResponse != null && retrofitResponse.isSuccessful()) {
            result = retrofitResponse.body();
        }
        return result;
    }

    private static OkHttpClient getOkHttp() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }
        return new OkHttpClient.Builder()
                .addInterceptor(new RetrofitInterceptor())
                .addInterceptor(interceptor)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }
}
