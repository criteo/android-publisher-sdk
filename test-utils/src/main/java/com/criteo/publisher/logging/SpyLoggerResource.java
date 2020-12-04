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

package com.criteo.publisher.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import androidx.annotation.NonNull;
import com.criteo.publisher.mock.DependencyProviderRef;
import com.criteo.publisher.mock.TestDependencyProvider;
import com.criteo.publisher.mock.TestResource;

public class SpyLoggerResource implements TestResource {

  @NonNull
  private final DependencyProviderRef dependencyProviderRef;

  private Logger spiedLogger;

  public SpyLoggerResource(@NonNull DependencyProviderRef dependencyProviderRef) {
    this.dependencyProviderRef = dependencyProviderRef;
  }

  @Override
  public void setUp() {
    TestDependencyProvider dependencyProvider = dependencyProviderRef.get();
    LoggerFactory mockLoggerFactory = mock(LoggerFactory.class);

    spiedLogger = spy(dependencyProvider.provideLoggerFactory().createLogger(getClass()));
    doReturn(spiedLogger).when(dependencyProvider).provideLogger();
    doReturn(spiedLogger).when(mockLoggerFactory).createLogger(any());
    doReturn(mockLoggerFactory).when(dependencyProvider).provideLoggerFactory();
  }

  @Override
  public void tearDown() {
    spiedLogger = null;
  }
}
