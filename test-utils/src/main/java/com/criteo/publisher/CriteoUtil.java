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
import com.criteo.publisher.application.InstrumentationUtil;
import com.criteo.publisher.model.AdUnit;
import java.util.Arrays;
import java.util.List;

public class CriteoUtil {

  public static final String TEST_CP_ID = "B-000001";
  public static final String PROD_CP_ID = "B-056946";
  public static final String PROD_CDB_URL = "https://bidder.criteo.com";

  public static Criteo givenInitializedCriteo(AdUnit... preloadedAdUnits)
      throws CriteoInitException {
    Application app = InstrumentationUtil.getApplication();

    clearCriteo();

    return new Criteo.Builder(app, TEST_CP_ID)
        .adUnits(Arrays.asList(preloadedAdUnits))
        .init();
  }

  /**
   * Clear any side effects from previous calls due to the Criteo singleton.
   */
  public static void clearCriteo() {
    Criteo.setInstance(null);
  }

  public static Criteo.Builder getCriteoBuilder(AdUnit... preloadedAdUnits) {
    Application app = InstrumentationUtil.getApplication();

    // clears any side effects from previous calls
    Criteo.setInstance(null);

    List<AdUnit> adUnits = Arrays.asList(preloadedAdUnits);
    return new Criteo.Builder(app, TEST_CP_ID).adUnits(adUnits);
  }

}
