package com.criteo.publisher.Util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.MockableDependencyProvider;
import com.criteo.publisher.TrackingCommandsExecutor;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.Executor;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Use this Rule when writing tests that require mocking global dependencies
 * See {@link com.criteo.publisher.degraded.StandaloneDegradedTest} for example.
 */
public class MockedDependenciesRule implements TestRule {
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
          base.evaluate();
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

  public ResultCaptor<Cdb> captorCdbResult() {
    ResultCaptor<Cdb> captor = new ResultCaptor<>();
    PubSdkApi spyApi = spy(getDependencyProvider().providePubSdkApi());
    doReturn(spyApi).when(getDependencyProvider()).providePubSdkApi();
    doAnswer(captor).when(spyApi).loadCdb(any(), any(), any());
    return captor;
  }
}
