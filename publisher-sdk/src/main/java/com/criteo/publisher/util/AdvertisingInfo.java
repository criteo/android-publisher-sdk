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
import android.util.Log;

public class AdvertisingInfo {

  private static final String GET_ADVERTISING_ID = "getId";
  private static final String IS_LIMIT_AD_TRACKING_ENABLED = "isLimitAdTrackingEnabled";

  public String getAdvertisingId(Context context) {
    try {
      return (String) ReflectionUtil.callAdvertisingIdInfo(GET_ADVERTISING_ID, context);
    } catch (Exception e) {
      Log.e("AdvertisingInfo", "Error getting advertising id: " + e.getMessage());
    }
    return null;
  }

  public boolean isLimitAdTrackingEnabled(Context context) {
    try {
      Object isLimitAdTrackingEnabled = ReflectionUtil
          .callAdvertisingIdInfo(IS_LIMIT_AD_TRACKING_ENABLED, context);
      return (boolean) isLimitAdTrackingEnabled;
    } catch (Exception e) {
      Log.e("AdvertisingInfo", "Error checking if ad tracking is limited: " + e.getMessage());
    }
    return false;
  }
}
