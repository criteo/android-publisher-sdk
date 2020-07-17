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

package com.criteo.publisher.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;

public class AdvertisingInfo {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final SafeAdvertisingIdClient advertisingIdClient = new SafeAdvertisingIdClient();

  @NonNull
  private final Context context;

  public AdvertisingInfo(@NonNull Context context) {
    this.context = context;
  }

  @Nullable
  public String getAdvertisingId() {
    try {
      return advertisingIdClient.getId(context);
    } catch (Exception e) {
      logger.debug("Error getting advertising id", e);
      return null;
    }
  }

  public boolean isLimitAdTrackingEnabled() {
    try {
      return advertisingIdClient.isLimitAdTrackingEnabled(context);
    } catch (Exception e) {
      logger.debug("Error checking if ad tracking is limited", e);
      return false;
    }
  }

  private static class SafeAdvertisingIdClient {

    public String getId(@NonNull Context context) throws Exception {
      try {
        Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        return advertisingIdInfo.getId();
      } catch (LinkageError e) {
        throw new MissingPlayServicesAdsIdentifierException(e);
      }
    }

    public boolean isLimitAdTrackingEnabled(@NonNull Context context) throws Exception {
      try {
        Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        return advertisingIdInfo.isLimitAdTrackingEnabled();
      } catch (LinkageError e) {
        throw new MissingPlayServicesAdsIdentifierException(e);
      }
    }

  }

  static class MissingPlayServicesAdsIdentifierException extends Exception {

    MissingPlayServicesAdsIdentifierException(Throwable cause) {
      super("play-services-ads-identifier does not seems to be in the classpath", cause);
    }
  }
}
