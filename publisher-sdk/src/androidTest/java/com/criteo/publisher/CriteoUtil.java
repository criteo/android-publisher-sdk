package com.criteo.publisher;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.model.AdUnit;
import java.util.Arrays;

public class CriteoUtil {

  public static final String TEST_CP_ID = "B-000001";

  public static Criteo givenInitializedCriteo(AdUnit... preloadedAdUnits) throws CriteoInitException {
    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    clearCriteo();

    return Criteo.init(app, TEST_CP_ID, Arrays.asList(preloadedAdUnits));
  }

  /**
   * Clear any side effects from previous calls
   */
  public static void clearCriteo() {
    Criteo.setInstance(null);
  }

}
