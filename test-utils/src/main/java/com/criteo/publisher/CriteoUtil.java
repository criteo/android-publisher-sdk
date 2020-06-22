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

package com.criteo.publisher;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.util.InstrumentationUtil;
import java.util.Arrays;
import java.util.List;

public class CriteoUtil {

  public static final String TEST_CP_ID = "B-000001";

  public static Criteo givenInitializedCriteo(AdUnit... preloadedAdUnits)
      throws CriteoInitException {
    Application app = InstrumentationUtil.getApplication();

    clearCriteo();

    return Criteo.init(app, TEST_CP_ID, Arrays.asList(preloadedAdUnits));
  }

  /**
   * Clear any side effects from previous calls due to the Criteo singleton.
   */
  public static void clearCriteo() {
    Criteo.setInstance(null);
  }

  public static Criteo.Builder getCriteoBuilder(AdUnit... preloadedAdUnits) {
    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    // clears any side effects from previous calls
    Criteo.setInstance(null);

    List<AdUnit> adUnits = Arrays.asList(preloadedAdUnits);
    return new Criteo.Builder(app, TEST_CP_ID).adUnits(adUnits);
  }

  /**
   * Clear all states retained in shared preferences used by the SDK.
   */
  public static void clearSharedPreferences() {
    SharedPreferences sharedPreferences = getSharedPreferences();

    sharedPreferences.edit().clear().apply();
  }

  private static SharedPreferences getSharedPreferences() {
    Context context = InstrumentationRegistry.getTargetContext().getApplicationContext();

    return context.getSharedPreferences(
        BuildConfig.pubSdkSharedPreferences,
        Context.MODE_PRIVATE);
  }

}
