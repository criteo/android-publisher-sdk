/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.network;

import static com.criteo.publisher.network.AppEventTask.THROTTLE;
import static com.criteo.publisher.util.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.privacy.gdpr.GdprData;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AppEventResponseListener;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

public class AppEventTaskTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule()
      .withSpiedLogger();

  private JSONObject json;

  @Mock
  private AppEventResponseListener responseListener;

  @Mock
  private Context context;

  @Mock
  private AdvertisingInfo advertisingInfo;

  @Mock
  private PubSdkApi api;

  @Mock
  private DeviceInfo deviceInfo;

  @Mock
  private UserPrivacyUtil userPrivacyUtil;

  @Mock
  private GdprData gdprData;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(deviceInfo.getUserAgent()).thenReturn(completedFuture("myUserAgent"));

    json = new JSONObject();
    givenApiReturning(json);
  }

  @Test
  public void testWithThrottleOnPostExecute() throws Exception {
    json.put(THROTTLE, 5);

    AppEventTask appEventTask = createTask("eventType");
    appEventTask.run();

    verify(responseListener, times(1)).setThrottle(5);
  }

  @Test
  public void testWithNullThrottle() throws Exception {
    json.remove(THROTTLE);

    AppEventTask appEventTask = createTask("eventType");
    appEventTask.run();

    verify(responseListener, times(1)).setThrottle(0);
  }

  @Test
  public void backgroundTask_GivenUserAgent_CallApiWithIt() throws Exception {
    when(deviceInfo.getUserAgent()).thenReturn(completedFuture("myUserAgent"));
    when(userPrivacyUtil.getGdprConsentData()).thenReturn("fakeConsentData");

    AppEventTask appEventTask = createTask("eventType");
    appEventTask.run();

    verify(api).postAppEvent(
        anyInt(),
        any(),
        any(),
        eq("eventType"),
        anyInt(),
        eq("myUserAgent"),
        eq("fakeConsentData")
    );
  }

  @Test
  public void backgroundTask_GivenNetworkError_LogError() throws Exception {
    Exception exception = new Exception();

    whenApiPostAppEvent().thenThrow(exception);

    AppEventTask appEventTask = createTask("eventType");
    appEventTask.run();

    verify(mockedDependenciesRule.getSpiedLogger()).log(argThat(
        logMessage -> logMessage.getThrowable().getCause() == exception
    ));
  }

  private void givenApiReturning(JSONObject json) throws Exception {
    whenApiPostAppEvent().thenReturn(json);
  }

  private OngoingStubbing<JSONObject> whenApiPostAppEvent() throws Exception {
    return when(api.postAppEvent(anyInt(), any(), any(), any(), anyInt(), any(), any()));
  }

  private AppEventTask createTask(String eventType) {
    return new AppEventTask(
        context,
        responseListener,
        advertisingInfo,
        api,
        deviceInfo,
        userPrivacyUtil,
        eventType
    );
  }

}