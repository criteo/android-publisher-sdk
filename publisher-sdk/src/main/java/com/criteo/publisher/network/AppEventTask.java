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
import androidx.annotation.NonNull;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AppEventResponseListener;
import org.json.JSONObject;

public class AppEventTask extends SafeRunnable {

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

  @NonNull
  private final String eventType;

  public AppEventTask(
      @NonNull Context context,
      @NonNull AppEventResponseListener responseListener,
      @NonNull AdvertisingInfo advertisingInfo,
      @NonNull PubSdkApi api,
      @NonNull DeviceInfo deviceInfo,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull String eventType
  ) {
    this.mContext = context;
    this.responseListener = responseListener;
    this.advertisingInfo = advertisingInfo;
    this.api = api;
    this.deviceInfo = deviceInfo;
    this.userPrivacyUtil = userPrivacyUtil;
    this.eventType = eventType;
  }

  @Override
  public void runSafely() throws Throwable {
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

    if (response.has(THROTTLE)) {
      responseListener.setThrottle(response.optInt(THROTTLE, 0));
    } else {
      responseListener.setThrottle(0);
    }
  }
}
