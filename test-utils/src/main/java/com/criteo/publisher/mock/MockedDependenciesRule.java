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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.os.Build.VERSION_CODES;
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
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.InstrumentationUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Use this Rule when writing tests that require mocking global dependencies.
 */
public class MockedDependenciesRule implements MethodRule {

  /**
   * Apply a timeout on all tests using this rule.
   * <p>
   * Lot of tests are waiting for end of AsyncTasks or for third-parties' events. Those tests may be
   * stuck and block the overall execution. In order to avoid any infinite blocking, long tests will
   * be killed by this timeout rule.
   * <p>
   * This timeout duration is roughly set and may have to be updated in the future.
   * <p>
   * Warning: When debugging, this timeout may interrupt your work and be annoying. To deactivate
   * it, you may set the {@link #iAmDebuggingDoNotTimeoutMe} variable to <code>true</code>.
   */
  private final FailOnTimeout.Builder timeout = FailOnTimeout.builder()
      .withTimeout(60, TimeUnit.SECONDS);

  private final boolean iAmDebuggingDoNotTimeoutMe = false; // Do not commit this set to true

  /**
   * If set to <code>true</code>, then a CDB mock server is instantiated and started before each
   * tests, and shutdown after.
   * <p>
   * This server answers like the preprod of CDB, except that it works only for Ad Units defined in
   * {@link com.criteo.publisher.TestAdUnits}.
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final boolean injectCdbMockServer = true;

  @Nullable
  private Logger mockedLogger = null;
  private boolean injectMockedLogger = false;
  private TrackingCommandsExecutor trackingCommandsExecutor;

  protected DependencyProvider dependencyProvider;
  private Object target;

  @Nullable
  private CdbMock cdbMock;

  /**
   * Activate mocking of {@link Logger}.
   * <p>
   * All loggers created through the {@link LoggerFactory} are mocked and represented by a single
   * instance given by {@link #getMockedLogger()}.
   * <p>
   * Note that this option should be used when creating the {@link org.junit.Rule}.
   *
   * @return this for chaining calls
   */
  public MockedDependenciesRule withMockedLogger() {
    injectMockedLogger = true;
    return this;
  }

  @Override
  public Statement apply(Statement base, FrameworkMethod method, Object target) {
    this.target = target;

    return new Statement() {
      @RequiresApi(api = VERSION_CODES.O)
      @Override
      public void evaluate() throws Throwable {
        try {
          resetAllDependencies();
          resetAllPersistedData();

          if (iAmDebuggingDoNotTimeoutMe) {
            base.evaluate();
          } else {
            timeout.build(base).evaluate();
          }
        } finally {
          resetAllPersistedData();

          // clean after self and ensures no side effects for subsequent tests
          MockableDependencyProvider.setInstance(null);

          if (cdbMock != null) {
            cdbMock.shutdown();
          }

          mockedLogger = null;
        }
      }
    };
  }

  private void setUpDependencyProvider() {
    DependencyProvider originalDependencyProvider = DependencyProvider.getInstance();

    Application application = InstrumentationUtil.getApplication();
    originalDependencyProvider.setApplication(application);
    originalDependencyProvider.setCriteoPublisherId(CriteoUtil.TEST_CP_ID);

    Executor oldExecutor = originalDependencyProvider.provideThreadPoolExecutor();

    trackingCommandsExecutor = new TrackingCommandsExecutor(oldExecutor);
    dependencyProvider = spy(originalDependencyProvider);
    MockableDependencyProvider.setInstance(dependencyProvider);
    doReturn(trackingCommandsExecutor).when(dependencyProvider).provideThreadPoolExecutor();
    doReturn(trackingCommandsExecutor.asAsyncResources()).when(dependencyProvider)
        .provideAsyncResources();
  }

  private void setUpCdbMock() {
    if (!injectCdbMockServer) {
      return;
    }

    cdbMock = new CdbMock(dependencyProvider.provideJsonSerializer());
    cdbMock.start();

    BuildConfigWrapper buildConfigWrapper = spy(dependencyProvider.provideBuildConfigWrapper());
    when(buildConfigWrapper.getCdbUrl()).thenReturn(cdbMock.getUrl());
    when(buildConfigWrapper.getEventUrl()).thenReturn(cdbMock.getUrl());
    when(dependencyProvider.provideBuildConfigWrapper()).thenReturn(buildConfigWrapper);
  }

  @Nullable
  public Logger getMockedLogger() {
    return mockedLogger;
  }

  private void setUpMockedLogger() {
    if (!injectMockedLogger) {
      return;
    }

    mockedLogger = mock(Logger.class);

    LoggerFactory loggerFactory = spy(dependencyProvider.provideLoggerFactory());
    doReturn(mockedLogger).when(loggerFactory).createLogger(any());
    when(dependencyProvider.provideLoggerFactory()).thenReturn(loggerFactory);
  }

  @RequiresApi(api = VERSION_CODES.O)
  private void injectDependencies() {
    DependenciesAnnotationInjection injection = new DependenciesAnnotationInjection(
        dependencyProvider);
    injection.process(target);
  }

  public DependencyProvider getDependencyProvider() {
    return dependencyProvider;
  }

  @RequiresApi(api = VERSION_CODES.M)
  public void waitForIdleState() {
    ThreadingUtil.waitForAllThreads(trackingCommandsExecutor);
  }

  public ResultCaptor<CdbResponse> captorCdbResult() {
    ResultCaptor<CdbResponse> captor = new ResultCaptor<>();
    PubSdkApi spyApi = spy(getDependencyProvider().providePubSdkApi());
    doReturn(spyApi).when(getDependencyProvider()).providePubSdkApi();

    try {
      doAnswer(captor).when(spyApi).loadCdb(any(), any());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return captor;
  }

  @NonNull
  public CdbMock getCdbMock() {
    if (cdbMock == null) {
      throw new IllegalStateException("CDB mock is only available while test is running");
    }
    return cdbMock;
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
    MockableDependencyProvider.setInstance(null);
    CriteoUtil.clearCriteo();

    if (cdbMock != null) {
      cdbMock.shutdown();
    }

    setUpDependencyProvider();
    setUpCdbMock();
    setUpMockedLogger();
    injectDependencies();
  }

  @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
  private void resetAllPersistedData() {
    // Clear all states retained in shared preferences used by the SDK.
    dependencyProvider.provideSharedPreferences().edit().clear().apply();

    // Clear CSM
    MetricHelper.cleanState(dependencyProvider);
  }
}
