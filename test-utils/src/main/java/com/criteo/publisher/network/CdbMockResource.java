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

package com.criteo.publisher.network;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.mock.DependencyProviderRef;
import com.criteo.publisher.mock.TestDependencyProvider;
import com.criteo.publisher.mock.TestResource;
import com.criteo.publisher.util.BuildConfigWrapper;

public class CdbMockResource implements TestResource {

  @NonNull
  private final DependencyProviderRef dependencyProviderRef;

  @Nullable
  private CdbMock cdbMock;

  public CdbMockResource(@NonNull DependencyProviderRef dependencyProviderRef) {
    this.dependencyProviderRef = dependencyProviderRef;
  }

  @Override
  public void setUp() {
    TestDependencyProvider dependencyProvider = dependencyProviderRef.get();
    cdbMock = new CdbMock(dependencyProvider.provideJsonSerializer());
    cdbMock.start();

    BuildConfigWrapper buildConfigWrapper = spy(dependencyProvider.provideBuildConfigWrapper());
    when(buildConfigWrapper.getCdbUrl()).thenReturn(cdbMock.getUrl());
    when(buildConfigWrapper.getEventUrl()).thenReturn(cdbMock.getUrl());
    when(dependencyProvider.provideBuildConfigWrapper()).thenReturn(buildConfigWrapper);

    doReturn(cdbMock).when(dependencyProvider).provideCdbMock();
  }

  @Override
  public void tearDown() {
    if (cdbMock != null) {
      cdbMock.shutdown();
    }

    cdbMock = null;
  }
}
