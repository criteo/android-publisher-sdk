package com.criteo.publisher.util;

import android.app.Application;
import android.os.Build.VERSION;
import androidx.test.core.app.ApplicationProvider;
import com.criteo.publisher.mock.ApplicationMock;

public class InstrumentationUtil {

  public static boolean isRunningInInstrumentationTest() {
    return VERSION.SDK_INT != 0;
  }

  public static Application getApplication() {
    if (isRunningInInstrumentationTest()) {
      return (Application) ApplicationProvider.getApplicationContext();
    } else {
      return ApplicationMock.newMock();
    }
  }

}
