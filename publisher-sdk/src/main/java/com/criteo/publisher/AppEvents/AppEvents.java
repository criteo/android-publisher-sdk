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
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.network.AppEventTask;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AppEventResponseListener;
import com.criteo.publisher.util.ApplicationStoppedListener;
import com.criteo.publisher.util.DeviceUtil;
import java.util.concurrent.Executor;

public class AppEvents implements AppEventResponseListener, ApplicationStoppedListener {

  private static final String EVENT_INACTIVE = "Inactive";
  private static final String EVENT_ACTIVE = "Active";
  private static final String EVENT_LAUNCH = "Launch";

  private AppEventTask eventTask;
  private final Context mContext;
  private int appEventThrottle = -1;
  private long throttleSetTime = 0;

  private final DeviceUtil deviceUtil;
  private final Clock clock;
  private final PubSdkApi api;
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final DeviceInfo deviceInfo;

  public AppEvents(
      @NonNull Context context,
      @NonNull DeviceUtil deviceUtil,
      @NonNull Clock clock,
      @NonNull PubSdkApi api,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull DeviceInfo deviceInfo
  ) {
    this.mContext = context;
    this.deviceUtil = deviceUtil;
    this.clock = clock;
    this.api = api;
    this.userPrivacyUtil = userPrivacyUtil;
    this.deviceInfo = deviceInfo;
    this.eventTask = createEventTask();
  }

  private void postAppEvent(String eventType) {
    if (shouldCallBearcat()) {
      if (appEventThrottle > 0 &&
          clock.getCurrentTimeInMillis() - throttleSetTime < appEventThrottle * 1000) {
        return;
      }
      if (eventTask.getStatus() == AsyncTask.Status.FINISHED) {
        eventTask = createEventTask();
      }
      if (eventTask.getStatus() != AsyncTask.Status.RUNNING) {
        Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
        eventTask.executeOnExecutor(threadPoolExecutor, eventType);
      }
    }
  }

  @NonNull
  private AppEventTask createEventTask() {
    return new AppEventTask(mContext, this, deviceUtil, api, deviceInfo, userPrivacyUtil);
  }

  @Override
  public void setThrottle(int throttle) {
    this.appEventThrottle = throttle;
    this.throttleSetTime = clock.getCurrentTimeInMillis();
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
