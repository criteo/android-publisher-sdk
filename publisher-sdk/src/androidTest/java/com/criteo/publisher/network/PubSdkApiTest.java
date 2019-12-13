package com.criteo.publisher.network;

import static junit.framework.Assert.assertNotNull;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.Util.MockedDependenciesRule;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class PubSdkApiTest {

    @Rule
    public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

    private int senderId;
    private int limitedAdTracking;
    private String gaid;
    private String eventType;
    private Context context;
    private String appId;
    private PubSdkApi api;


    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        appId = context.getApplicationContext().getPackageName();
        senderId = 2379;
        limitedAdTracking = 0;
        gaid = "021a86de-ef82-4f69-867b-61ca66688c9c";
        eventType = "Launch";
        api = mockedDependenciesRule.getDependencyProvider().providePubSdkApi();
    }


    @Test
    public void testPostAppEventWithNullGaid() {
        JSONObject object = api
            .postAppEvent(context, senderId, appId, gaid, eventType, limitedAdTracking);
        assertNotNull(object);
    }

    @Test
    public void testPostAppEventWithGaid() {
        gaid = null;
        JSONObject object = api
            .postAppEvent(context, senderId, appId, gaid, eventType, limitedAdTracking);
        assertNotNull(object);
    }
}