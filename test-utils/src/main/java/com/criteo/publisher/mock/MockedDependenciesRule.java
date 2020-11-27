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

package com.criteo.publisher.mock;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.os.Build.VERSION_CODES;
import android.os.Debug;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.MockableDependencyProvider;
import com.criteo.publisher.concurrent.ThreadingUtil;
import com.criteo.publisher.concurrent.TrackingCommandsExecutor;
import com.criteo.publisher.csm.MetricHelper;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.mock.TestResource.CompositeTestResource;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.InstrumentationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.stubbing.Answer;

/**
 * Use this Rule when writing tests that require mocking global dependencies.
 */
public class MockedDependenciesRule implements MethodRule {

  /**
   * Apply a timeout on all tests using this rule.
   * <p>
   * Lot of tests are waiting for the end of AsyncTasks or for third-parties' events. Those tests may be stuck and block
   * the overall execution. In order to avoid any infinite blocking, long tests will be killed by this timeout rule.
   * <p>
   * This timeout duration is roughly set and may have to be updated in the future.
   * <p>
   * Warning: When debugging, this timeout is deactivated to not interrupt your work. To activate it, you may set the
   * {@link #iAmDebuggingDoNotTimeoutMe} variable to <code>false</code>.
   */
  private final FailOnTimeout.Builder timeout = FailOnTimeout.builder()
      .withTimeout(60, TimeUnit.SECONDS);

  private final boolean iAmDebuggingDoNotTimeoutMe = Debug.isDebuggerConnected();

  /**
   * If set to <code>true</code>, then a CDB mock server is instantiated and started before each
   * tests, and shutdown after.
   * <p>
   * This server answers like the preprod of CDB, except that it works only for Ad Units defined in
   * {@link com.criteo.publisher.TestAdUnits}.
   */
  private boolean injectCdbMockServer = true;

  private boolean injectSpiedLogger = false;

  @NonNull
  private final DependencyProviderRef dependencyProviderRef;

  @Nullable
  private Object testInstance;

  @Nullable
  private MultiThreadResource multiThreadResource;

  @Nullable
  private SpyLoggerResource spyLoggerResource;

  @Nullable
  private TestResource inMemoryResource;

  public MockedDependenciesRule() {
    this.dependencyProviderRef = new DependencyProviderRef();
  }

  @NonNull
  private TestResource getInMemoryResource() {
    if (inMemoryResource == null) {
      // Order of creation is important: resources are setUp in this order before every test and tearDown in the reverse
      // order after.
      // Tests are isolated to remove side effect. Some code use the dependency provider singleton: so if a previous
      // test is still living, it can use a dependency provider of a new test and provide unexpected side effect.
      //
      // So setUp is:
      // - dependency provider singleton is injected
      // - threads are managed
      // - Application singleton is registered in dependency provider
      // - order of CDB mock and logger is not important
      //
      // Then tearDown is:
      // - order of CDB mock and logger is not important
      // - Application singleton is cleaned
      // - threads are synchronized to get an idle state
      // - dependency provider singleton is cleaned
      // - internal state is cleaned

      List<TestResource> resources = new ArrayList<>();
      resources.add(new DependencyProviderResource(dependencyProviderRef));
      resources.add(getMultiThreadResource());
      resources.add(new ApplicationResource(dependencyProviderRef));

      if (injectCdbMockServer) {
        resources.add(new CdbMockResource(dependencyProviderRef));
      }

      if (injectSpiedLogger) {
        spyLoggerResource = new SpyLoggerResource(dependencyProviderRef);
        resources.add(spyLoggerResource);
      }

      inMemoryResource = new CompositeTestResource(resources);
    }
    return inMemoryResource;
  }

  private MultiThreadResource getMultiThreadResource() {
    if (multiThreadResource == null) {
       multiThreadResource = new MultiThreadResource(dependencyProviderRef);
    }
    return multiThreadResource;
  }

  @Override
  public Statement apply(Statement base, FrameworkMethod method, Object target) {
    return new Statement() {
      @RequiresApi(api = VERSION_CODES.O)
      @Override
      public void evaluate() throws Throwable {
        Throwable throwable = null;
        try {
          testInstance = target;

          resetAllDependencies();
          resetAllPersistedData();

          if (iAmDebuggingDoNotTimeoutMe) {
            base.evaluate();
          } else {
            timeout.build(base).evaluate();
          }
        } catch (Throwable t) {
          throwable = t;
        } finally {
          try {
            resetAllPersistedData();
            getInMemoryResource().tearDown();
          } catch (Throwable t) {
            if (throwable != null) {
              throwable.addSuppressed(t);
            } else {
              throwable = t;
            }
          }
        }

        if (throwable != null) {
          throw throwable;
        }
      }
    };
  }

  protected TestDependencyProvider createDependencyProvider() {
    return new TestDependencyProvider();
  }

  /**
   * Activate spying of {@link Logger}.
   * <p>
   * All loggers created through the {@link LoggerFactory} are spied and represented by a single
   * instance given by {@link TestDependencyProvider#provideLogger()}. You can obtain it via annotation injection (see
   * {@link DependenciesAnnotationInjection}).
   * <p>
   * Note that this option should be used when creating the {@link org.junit.Rule}.
   *
   * @return this for chaining calls
   */
  public MockedDependenciesRule withSpiedLogger() {
    injectSpiedLogger = true;
    clearInternalState();
    return this;
  }

  public MockedDependenciesRule withoutCdbMock() {
    injectCdbMockServer = false;
    clearInternalState();
    return this;
  }

  public DependencyProvider getDependencyProvider() {
    return dependencyProviderRef.get();
  }

  /**
   * Setup a {@link ResultCaptor} to captur CDB response.
   *
   * The {@link PubSdkApi} in this {@link #getDependencyProvider() dependency provider} should already be a mock or a
   * spy.
   */
  public ResultCaptor<CdbResponse> captorCdbResult() {
    ResultCaptor<CdbResponse> captor = new ResultCaptor<>();
    PubSdkApi spyApi = getDependencyProvider().providePubSdkApi();

    try {
      doAnswer(captor).when(spyApi).loadCdb(any(), any());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return captor;
  }

  @RequiresApi(api = VERSION_CODES.M)
  public void waitForIdleState() {
    getMultiThreadResource().waitForIdleState();
  }

  /**
   * Clean in-memory state of all dependencies, including the dependency provider itself and the
   * criteo singleton.
   * <p>
   * This simulate a restart of an application with a new SDK instance.
   * <p>
   * Note that persistent data such as shared preferences are not cleaned.
   * <p>
   * If calling test is injecting dependencies via annotation, then after this call, all
   * dependencies fields would get updated with new ones.
   */
  @RequiresApi(api = VERSION_CODES.O)
  public void resetAllDependencies() {
    getInMemoryResource().tearDown();

    getInMemoryResource().setUp();
    setUpInjectedDependencies();

    if (spyLoggerResource != null) {
      spyLoggerResource.finishSetup();
    }
  }

  @RequiresApi(api = VERSION_CODES.O)
  private void setUpInjectedDependencies() {
    assert testInstance != null;
    DependenciesAnnotationInjection injection = new DependenciesAnnotationInjection(dependencyProviderRef.get());
    injection.process(testInstance);
  }

  @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
  private void resetAllPersistedData() {
    DependencyProvider dependencyProvider = dependencyProviderRef.get();

    // Clear all states retained in shared preferences used by the SDK.
    dependencyProvider.provideSharedPreferences().edit().clear().apply();

    // Clear CSM
    MetricHelper.cleanState(dependencyProvider);
  }

  private void clearInternalState() {
    multiThreadResource = null;
    spyLoggerResource = null;
    inMemoryResource = null;
  }

  private static class CdbMockResource implements TestResource {

    @NonNull
    private final DependencyProviderRef dependencyProviderRef;

    @Nullable
    private CdbMock cdbMock;

    private CdbMockResource(@NonNull DependencyProviderRef dependencyProviderRef) {
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

  private static class MultiThreadResource implements TestResource {

    @NonNull
    private final DependencyProviderRef dependencyProviderRef;

    @Nullable
    private TrackingCommandsExecutor trackingCommandsExecutor;

    private MultiThreadResource(@NonNull DependencyProviderRef dependencyProviderRef) {
      this.dependencyProviderRef = dependencyProviderRef;
    }

    @RequiresApi(api = VERSION_CODES.M)
    void waitForIdleState() {
      if (trackingCommandsExecutor != null) {
        ThreadingUtil.waitForAllThreads(trackingCommandsExecutor);
      }
    }

    @Override
    public void setUp() {
      DependencyProvider dependencyProvider = dependencyProviderRef.get();
      Executor oldExecutor = dependencyProvider.provideThreadPoolExecutor();

      trackingCommandsExecutor = new TrackingCommandsExecutor(oldExecutor);
      doReturn(trackingCommandsExecutor).when(dependencyProvider).provideThreadPoolExecutor();
      doReturn(trackingCommandsExecutor.asAsyncResources()).when(dependencyProvider).provideAsyncResources();
    }

    @Override
    @SuppressLint("NewApi")
    public void tearDown() {
      // Wait for an idle state so we're sure that the previous test won't have any impact on the next tests.
      waitForIdleState();
      trackingCommandsExecutor = null;
    }
  }

  private static class SpyLoggerResource implements TestResource {

    @NonNull
    private final DependencyProviderRef dependencyProviderRef;

    private Logger spiedLogger;

    private SpyLoggerResource(@NonNull DependencyProviderRef dependencyProviderRef) {
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
    void finishSetup() {
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

  private class DependencyProviderResource implements TestResource {

    @NonNull
    private final DependencyProviderRef dependencyProviderRef;

    private DependencyProviderResource(@NonNull DependencyProviderRef dependencyProviderRef) {
      this.dependencyProviderRef = dependencyProviderRef;
    }

    @Override
    public void setUp() {
      TestDependencyProvider dependencyProvider = spy(createDependencyProvider());
      MockableDependencyProvider.setInstance(dependencyProvider);
      dependencyProviderRef.set(dependencyProvider);
    }

    @Override
    public void tearDown() {
      CriteoUtil.clearCriteo();
      MockableDependencyProvider.setInstance(null);
      dependencyProviderRef.clear();
    }
  }

  private static class ApplicationResource implements TestResource {

    @NonNull
    private final DependencyProviderRef dependencyProviderRef;

    private ApplicationResource(@NonNull DependencyProviderRef dependencyProviderRef) {
      this.dependencyProviderRef = dependencyProviderRef;
    }

    @Override
    public void setUp() {
      DependencyProvider dependencyProvider = dependencyProviderRef.get();
      dependencyProvider.setApplication(InstrumentationUtil.getApplication());
      dependencyProvider.setCriteoPublisherId(CriteoUtil.TEST_CP_ID);
    }

    @Override
    public void tearDown() {
      // Many callbacks can be registered to an application: for instance, the GUM calls are sent because of such
      // callbacks. Then at the end of a test session, all callbacks are unregistered so they can't affect next tests.
      UnregisteringApplication.unregisterAllActivityLifecycleCallbacks();
    }
  }
}
