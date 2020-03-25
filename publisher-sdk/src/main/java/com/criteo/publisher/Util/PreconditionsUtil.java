package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import android.util.Log;
import com.criteo.publisher.DependencyProvider;

public class PreconditionsUtil {
  private static final String TAG = PreconditionsUtil.class.getSimpleName();

  /**
   * Throw a runtime exception and log if the SDK runs in debug mode, or just log otherwise
   */
  public static void throwOrLog(@NonNull Exception exception) {
    Log.w(TAG, exception);

    if (DependencyProvider.getInstance().provideBuildConfigWrapper().isDebug()) {
      throw new RuntimeException(exception);
    }
  }
}
