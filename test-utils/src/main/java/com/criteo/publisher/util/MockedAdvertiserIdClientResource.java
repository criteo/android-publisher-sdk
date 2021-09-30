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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import com.criteo.publisher.mock.DependencyProviderRef;
import com.criteo.publisher.mock.TestDependencyProvider;
import com.criteo.publisher.mock.TestResource;
import com.criteo.publisher.util.AdvertisingInfo.AdvertisingIdResult;
import com.criteo.publisher.util.AdvertisingInfo.SafeAdvertisingIdClient;

public class MockedAdvertiserIdClientResource implements TestResource {

  @SuppressLint("VisibleForTests")
  private static final AdvertisingIdResult ADVERTISING_ID_RESULT = new AdvertisingIdResult("cdda802e-fb9c-47ad-9866-0794d394c912", false);

  private final DependencyProviderRef dependencyProviderRef;

  public MockedAdvertiserIdClientResource(DependencyProviderRef dependencyProviderRef) {
    this.dependencyProviderRef = dependencyProviderRef;
  }

  @Override
  public void setUp() {
    TestDependencyProvider dependencyProvider = dependencyProviderRef.get();
    SafeAdvertisingIdClient mockAdvertisingIdClient = mock(SafeAdvertisingIdClient.class);

    try {
      when(mockAdvertisingIdClient.getAdvertisingIdResult(any())).thenReturn(ADVERTISING_ID_RESULT);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    doReturn(mockAdvertisingIdClient).when(dependencyProvider).provideSafeAdvertisingIdClient();
  }

  @Override
  public void tearDown() {
    // no-op
  }
}
