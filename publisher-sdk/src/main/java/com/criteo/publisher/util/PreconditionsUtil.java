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

import android.util.Log;
import androidx.annotation.NonNull;
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
