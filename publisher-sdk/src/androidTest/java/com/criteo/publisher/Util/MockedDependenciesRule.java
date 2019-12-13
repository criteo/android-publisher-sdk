package com.criteo.publisher.Util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.MockableDependencyProvider;
import com.criteo.publisher.TrackingCommandsExecutor;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Use this Rule when writing tests that require mocking global dependencies
 * See {@link com.criteo.publisher.degraded.StandaloneDegradedTest} for example.
 */
public class MockedDependenciesRule implements TestRule {

  /**
   * Apply a timeout on all tests using this rule.
   *
   * Lot of tests are waiting for end of AsyncTasks or for third-parties' events. Those tests may
   * be stuck and block the overall execution. In order to avoid any infinite blocking, long tests
   * will be killed by this timeout rule.
   *
   * This timeout duration is roughly set and may have to be updated in the future.
   *
   * Warning: When debugging, this timeout may interrupt your work and be annoying. To deactivate
   * it, you may set the {@link #iAmDebuggingDoNotTimeoutMe} variable to <code>true</code>.
   */
  private final Timeout timeout = Timeout.builder()
      .withTimeout(20, TimeUnit.SECONDS)
      .build();

  private final boolean iAmDebuggingDoNotTimeoutMe = false;

  protected DependencyProvider dependencyProvider;
  private TrackingCommandsExecutor trackingCommandsExecutor = null;

  @Override
  public Statement apply(Statement base, Description description) {
    try {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          DependencyProvider originalDependencyProvider = DependencyProvider.getInstance();
          Executor oldExecutor = originalDependencyProvider.provideThreadPoolExecutor();
          trackingCommandsExecutor = new TrackingCommandsExecutor(oldExecutor);
          dependencyProvider = spy(originalDependencyProvider);
          MockableDependencyProvider.setInstance(dependencyProvider);
          doReturn(trackingCommandsExecutor).when(dependencyProvider).provideThreadPoolExecutor();
          doReturn(trackingCommandsExecutor).when(dependencyProvider).provideSerialExecutor();

          if (iAmDebuggingDoNotTimeoutMe) {
            base.evaluate();
          } else {
            timeout.apply(base, description).evaluate();
          }
        }
      };
    } finally {
      // clean after self and ensures no side effects for subsequent tests
      MockableDependencyProvider.setInstance(null);
    }
  }

  public DependencyProvider getDependencyProvider() {
    return dependencyProvider;
  }

  public TrackingCommandsExecutor getTrackingCommandsExecutor() {
    return trackingCommandsExecutor;
  }

  public ResultCaptor<Cdb> captorCdbResult(Context context) {
    ResultCaptor<Cdb> captor = new ResultCaptor<>();
    PubSdkApi spyApi = spy(getDependencyProvider().providePubSdkApi(context));
    doReturn(spyApi).when(getDependencyProvider()).providePubSdkApi(any());
    doAnswer(captor).when(spyApi).loadCdb(any(), any());
    return captor;
  }
}
