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

package com.criteo.publisher.application;

import androidx.annotation.NonNull;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.mock.DependencyProviderRef;
import com.criteo.publisher.mock.TestResource;

public class ApplicationResource implements TestResource {

  @NonNull
  private final DependencyProviderRef dependencyProviderRef;

  public ApplicationResource(@NonNull DependencyProviderRef dependencyProviderRef) {
    this.dependencyProviderRef = dependencyProviderRef;
  }

  @Override
  public void setUp() {
    DependencyProvider dependencyProvider = dependencyProviderRef.get();
    dependencyProvider.setApplication(InstrumentationUtil.getApplication());
    dependencyProvider.setCriteoPublisherId(CriteoUtil.TEST_CP_ID);
    dependencyProvider.setInventoryGroupId(CriteoUtil.TEST_INVENTORY_GROUP_ID);
  }

  @Override
  public void tearDown() {
    // Many callbacks can be registered to an application: for instance, the GUM calls are sent because of such
    // callbacks. Then at the end of a test session, all callbacks are unregistered so they can't affect next tests.
    UnregisteringApplication.unregisterAllActivityLifecycleCallbacks();
  }
}
