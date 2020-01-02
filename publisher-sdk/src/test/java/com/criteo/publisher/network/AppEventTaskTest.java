package com.criteo.publisher.network;

import static com.criteo.publisher.network.AppEventTask.THROTTLE;

import android.content.Context;
import com.criteo.publisher.Util.AppEventResponseListener;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.model.DeviceInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AppEventTaskTest {

    private AppEventTask appEventTask;

    private JSONObject json;

    @Mock
    private AppEventResponseListener responseListener;

    @Mock
    private Context context;

    @Mock
    private DeviceUtil deviceUtil;

    @Mock
    private PubSdkApi api;

    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        appEventTask = new AppEventTask(context, responseListener, deviceUtil, api, deviceInfo);
        json = new JSONObject();
    }


    @Test
    public void testWithThrottleOnPostExecute() throws JSONException {
        json.put(THROTTLE, 5);
        appEventTask.onPostExecute(json);
        Mockito.verify(responseListener, Mockito.times(1)).setThrottle(json.optInt(THROTTLE, 0));
    }

    @Test
    public void testWithNullThrottleOnPostExecute() {
        appEventTask.onPostExecute(json);
        Mockito.verify(responseListener, Mockito.times(1)).setThrottle(0);
    }

    @Test
    public void testWithNullJsonOnPostExecute() {
        json = null;
        appEventTask.onPostExecute(json);
        Mockito.verify(responseListener, Mockito.times(1)).setThrottle(0);
    }

}