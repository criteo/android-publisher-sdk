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

package com.criteo.publisher.AppEvents;

import android.content.Context;
import androidx.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.network.AppEventTask;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AppEventResponseListener;
import com.criteo.publisher.util.ApplicationStoppedListener;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public class AppEvents implements AppEventResponseListener, ApplicationStoppedListener {

  private static final String EVENT_INACTIVE = "Inactive";
  private static final String EVENT_ACTIVE = "Active";
  private static final String EVENT_LAUNCH = "Launch";

  private final Context mContext;

  private final AdvertisingInfo advertisingInfo;
  private final Clock clock;
  private final PubSdkApi api;
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final Executor executor;

  private final AtomicLong silencedUntilTimeInMillis = new AtomicLong(-1);

  public AppEvents(
      @NonNull Context context,
      @NonNull AdvertisingInfo advertisingInfo,
      @NonNull Clock clock,
      @NonNull PubSdkApi api,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull DeviceInfo deviceInfo,
      @NonNull Executor executor
  ) {
    this.mContext = context;
    this.advertisingInfo = advertisingInfo;
    this.clock = clock;
    this.api = api;
    this.userPrivacyUtil = userPrivacyUtil;
    this.deviceInfo = deviceInfo;
    this.executor = executor;
  }

  private void postAppEvent(String eventType) {
    if (!shouldCallBearcat()) {
      return;
    }

    long silencedUntil = silencedUntilTimeInMillis.get();
    if (silencedUntil > 0 && clock.getCurrentTimeInMillis() < silencedUntil) {
      return;
    }

    executor.execute(new AppEventTask(
        mContext,
        this,
        advertisingInfo,
        api,
        deviceInfo,
        userPrivacyUtil,
        eventType
    ));
  }

  @Override
  public void setThrottle(int throttleInSec) {
    this.silencedUntilTimeInMillis.set(clock.getCurrentTimeInMillis() + throttleInSec * 1000);
  }

  public void sendLaunchEvent() {
    postAppEvent(EVENT_LAUNCH);
  }

  public void sendActiveEvent() {
    postAppEvent(EVENT_ACTIVE);
  }

  public void sendInactiveEvent() {
    postAppEvent(EVENT_INACTIVE);
  }

  @Override
  public void onApplicationStopped() {
  }

  private boolean shouldCallBearcat() {
    return userPrivacyUtil.isCCPAConsentGivenOrNotApplicable() && userPrivacyUtil
        .isMopubConsentGivenOrNotApplicable();
  }
}
