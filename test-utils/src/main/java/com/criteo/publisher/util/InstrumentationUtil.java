package com.criteo.publisher.util;

import android.os.Build.VERSION;

public class InstrumentationUtil {

  public static boolean isRunningInInstrumentationTest() {
    return VERSION.SDK_INT != 0;
  }

}
