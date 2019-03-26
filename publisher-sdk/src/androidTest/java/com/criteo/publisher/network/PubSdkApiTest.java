package com.criteo.publisher.network;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.criteo.publisher.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.criteo.publisher.network.PubSdkApi.executeGet;
import static com.criteo.publisher.network.PubSdkApi.getParamsString;
import static junit.framework.Assert.assertNotNull;


@RunWith(AndroidJUnit4.class)
public class PubSdkApiTest {
    private int senderId;
    private int limitedAdTracking;
    private String gaid;
    private String eventType;
    private Context context;
    private String appId;


    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        appId = context.getApplicationContext().getPackageName();
        senderId = 2379;
        limitedAdTracking = 0;
        gaid = "021a86de-ef82-4f69-867b-61ca66688c9c";
        eventType = "Launch";
    }


    @Test
    public void testPostAppEventWithNullGaid() {
        JSONObject object = PubSdkApi.postAppEvent(context, senderId, appId, gaid, eventType, limitedAdTracking);
        assertNotNull(object);
    }

    @Test
    public void testPostAppEventWithGaid() {
        gaid = null;
        JSONObject object = PubSdkApi.postAppEvent(context, senderId, appId, gaid, eventType, limitedAdTracking);
        assertNotNull(object);
    }
}