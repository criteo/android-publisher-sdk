package com.criteo.publisher;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import java.util.Collections;

public class CriteoUtil {

  public static Criteo givenInitializedCriteo() throws CriteoInitException {
    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    return Criteo.init(app, "B-056946", Collections.emptyList());
  }

}
