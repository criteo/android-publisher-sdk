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

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import androidx.annotation.NonNull;
import com.criteo.publisher.mock.DependencyProviderRef;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.TestDependencyProvider;
import com.criteo.publisher.mock.TestResource;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.stubbing.Answer;

public class SpyLoggerResource implements TestResource {

  @NonNull
  private final DependencyProviderRef dependencyProviderRef;

  private Logger spiedLogger;

  public SpyLoggerResource(@NonNull DependencyProviderRef dependencyProviderRef) {
    this.dependencyProviderRef = dependencyProviderRef;
  }

  @Override
  public void setUp() {
    /*
     Special care needs to be taken when mocking the logger:
     - Logger depends on beans such as the ConsoleHandler
     - (2) Other beans depends on Logger via LoggerFactory#createLogger which is called during beans' creation.

     When @SpyBean/@MockBean/@Injected beans are injected, loggers are created (2). So the logger factory should
     already be mocked to serve a spy/mock logger.
     But the logger factory should not be created before the injection step because its dependencies (1) would not be
     injected properly.

     So we have:
     - Logger should be mocked before injection step
     - LoggerFactory should be created after injection step

     This implementation is creating first a mocked logger before injection step. This is the logger that will be
     provided to other beans. The LoggerFactory is not created, only mocked.
     When mocked logger is used (so after the injection step), then one real LoggerFactory is created and used to create
     one real logger. Then the mocked logger delegates to the real logger.
    */

    TestDependencyProvider dependencyProvider = dependencyProviderRef.get();
    LoggerFactory mockLoggerFactory = mock(LoggerFactory.class);

    AtomicBoolean isFetchingRealLogger = new AtomicBoolean(false);
    AtomicReference<Answer<?>> lazyDelegateAnswerRef = new AtomicReference<>();
    spiedLogger = mock(Logger.class, invocation -> {
      if (lazyDelegateAnswerRef.get() == null) {
        isFetchingRealLogger.set(true);
        dependencyProvider.provideLoggerFactory();
      }
      return lazyDelegateAnswerRef.get().answer(invocation);
    });
    doReturn(spiedLogger).when(mockLoggerFactory).createLogger(any());

    doAnswer(invocation -> {
      if (isFetchingRealLogger.compareAndSet(true, false)) {
        LoggerFactory realLoggerFactory = (LoggerFactory) invocation.callRealMethod();
        Logger realLogger = realLoggerFactory.createLogger(MockedDependenciesRule.class);
        lazyDelegateAnswerRef.compareAndSet(null, delegatesTo(realLogger));
      }
      return mockLoggerFactory;
    }).when(dependencyProvider).provideLoggerFactory();

    doReturn(spiedLogger).when(dependencyProvider).provideLogger();
  }

  /**
   * Interact with logger to force generation of spied logger as described in {@link #setUp()}.
   * This is done here, while there is only a single thread, because Mockito is not thread-safe during stubbing.
   */
  public void finishSetup() {
    if (spiedLogger != null) {
      //noinspection ResultOfMethodCallIgnored
      spiedLogger.toString();
    }
  }

  @Override
  public void tearDown() {
    spiedLogger = null;
  }
}
