package com.criteo.publisher.util;

import androidx.annotation.NonNull;
import android.util.Log;
import com.criteo.publisher.DependencyProvider;

public class PreconditionsUtil {
  private static final String TAG = PreconditionsUtil.class.getSimpleName();

  /**
   * Throw a runtime exception and log if the SDK runs in debug mode, or just log otherwise
   */
  public static void throwOrLog(@NonNull Throwable exception) {
    Log.w(TAG, exception);

    if (DependencyProvider.getInstance().provideBuildConfigWrapper().preconditionThrowsOnException()) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Precondition used to assert that the given value is not null.
   * <p>
   * If the SDK is in debug mode, a {@link NullPointerException} is thrown to indicate that the
   * given value is null and violates this precondition. In release mode, only a warning is logged.
   * So you should not assume that the verified value is non null after this call.
   * <p>
   * This returns <code>true</code> if the given value is, as expected, not null, else it returns
   * <code>false</code>. Hence you can use this method in a if condition to assume the non
   * nullability in the if block:
   * <pre><code>
   *   Foo bar = foobar();
   *   if (PreconditionsUtil.isNotNull(bar)) {
   *     bar.baz(); // Safe, bar is not null here
   *   }
   * </code></pre>
   *
   * @param value value to test the nullability
   * @return <code>true</code> if given value is not null
   */
  public static boolean isNotNull(Object value) {
    if (value == null) {
      throwOrLog(new NullPointerException("Expected non null value, but null occurs."));
      return false;
    }
    return true;
  }
}
