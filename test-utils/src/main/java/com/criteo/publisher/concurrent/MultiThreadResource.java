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

package com.criteo.publisher.concurrent;

import android.annotation.SuppressLint;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.mock.DependencyProviderRef;
import com.criteo.publisher.mock.TestDependencyProvider;
import com.criteo.publisher.mock.TestResource;
import java.util.concurrent.Executor;

public class MultiThreadResource implements TestResource {

  @NonNull
  private final DependencyProviderRef dependencyProviderRef;

  @Nullable
  private TrackingCommandsExecutor trackingCommandsExecutor;

  public MultiThreadResource(@NonNull DependencyProviderRef dependencyProviderRef) {
    this.dependencyProviderRef = dependencyProviderRef;
  }

  @RequiresApi(api = VERSION_CODES.M)
  public void waitForIdleState() {
    if (trackingCommandsExecutor != null) {
      ThreadingUtil.waitForAllThreads(trackingCommandsExecutor);
    }
  }

  @Override
  public void setUp() {
    TestDependencyProvider dependencyProvider = dependencyProviderRef.get();
    Executor oldExecutor = dependencyProvider.provideThreadPoolExecutor();

    trackingCommandsExecutor = new TrackingCommandsExecutor(oldExecutor);

    dependencyProvider.inject(Executor.class, trackingCommandsExecutor);
    dependencyProvider.inject(AsyncResources.class, trackingCommandsExecutor.asAsyncResources());
  }

  @Override
  @SuppressLint("NewApi")
  public void tearDown() {
    // Many callbacks can be registered to an application: for instance, the GUM calls are sent because of such
    // callbacks. Then at the end of a test session, all callbacks are unregistered so they can't affect next tests.
    waitForIdleState();
    trackingCommandsExecutor = null;
  }
}
