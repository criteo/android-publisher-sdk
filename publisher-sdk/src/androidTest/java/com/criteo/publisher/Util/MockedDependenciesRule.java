package com.criteo.publisher.Util;

import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.MockableDependencyProvider;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

/**
 * Use this Rule when writing tests that require mocking global dependencies
 * See {@link com.criteo.publisher.degraded.StandaloneDegradedTest} for example.
 */
public class MockedDependenciesRule implements TestRule {
  protected DependencyProvider dependencyProvider;

  @Override
  public Statement apply(Statement base, Description description) {
    try {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          dependencyProvider = Mockito.mock(DependencyProvider.class);
          MockableDependencyProvider.setInstance(dependencyProvider);
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
}
