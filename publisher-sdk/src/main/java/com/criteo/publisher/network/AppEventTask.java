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

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AppEventResponseListener;
import org.json.JSONObject;

public class AppEventTask extends AsyncTask<Object, Void, JSONObject> {

  private static final int SENDER_ID = 2379;
  protected static final String THROTTLE = "throttleSec";

  private final Logger logger = LoggerFactory.getLogger(AppEventTask.class);

  @NonNull
  private final Context mContext;

  @NonNull
  private final AppEventResponseListener responseListener;

  @NonNull
  private final AdvertisingInfo advertisingInfo;

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final UserPrivacyUtil userPrivacyUtil;

  public AppEventTask(
      @NonNull Context context,
      @NonNull AppEventResponseListener responseListener,
      @NonNull AdvertisingInfo advertisingInfo,
      @NonNull PubSdkApi api,
      @NonNull DeviceInfo deviceInfo,
      @NonNull UserPrivacyUtil userPrivacyUtil
  ) {
    this.mContext = context;
    this.responseListener = responseListener;
    this.advertisingInfo = advertisingInfo;
    this.api = api;
    this.deviceInfo = deviceInfo;
    this.userPrivacyUtil = userPrivacyUtil;
  }

  @Override
  protected JSONObject doInBackground(Object... objects) {
    JSONObject jsonObject = null;

    try {
      jsonObject = doAppEventTask(objects);
    } catch (Throwable tr) {
      logger.debug("Internal AET exec error.", tr);
    }

    return jsonObject;
  }

  private JSONObject doAppEventTask(Object[] objects) throws Exception {
    String eventType = (String) objects[0];
    int limitedAdTracking = advertisingInfo.isLimitAdTrackingEnabled() ? 1 : 0;
    String gaid = advertisingInfo.getAdvertisingId();
    String appId = mContext.getPackageName();

    String userAgent = deviceInfo.getUserAgent().get();
    JSONObject response = api.postAppEvent(
        SENDER_ID,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        userAgent,
        userPrivacyUtil.getGdprData()
    );

    logger.debug("App event response: %s", response);

    return response;
  }

  @Override
  protected void onPostExecute(@Nullable JSONObject result) {
    new SafeRunnable() {
      @Override
      public void runSafely() {
        doOnPostExecute(result);
      }
    }.run();
  }

  private void doOnPostExecute(@Nullable JSONObject result) {
    super.onPostExecute(result);

    if (result != null && result.has(THROTTLE)) {
      responseListener.setThrottle(result.optInt(THROTTLE, 0));
    } else {
      responseListener.setThrottle(0);
    }
  }
}
