package com.criteo.publisher.degraded;

import android.os.Build.VERSION;
import org.junit.AssumptionViolatedException;

public class DegradedUtil {

  public static void assumeIsDegraded() {
    if (VERSION.SDK_INT >= 19) {
      throw new AssumptionViolatedException(
          "Functionality is not degraded, version of device should be < 19");
    }
  }

}
