package com.criteo.publisher;


import androidx.annotation.Nullable;

/**
 * This class is purposefully located within the <code>com.criteo.publisher</code> package, as it
 * needs to access the package private setter {@link DependencyProvider#setInstance(DependencyProvider)}
 */
public class MockableDependencyProvider {

  public static synchronized void setInstance(@Nullable DependencyProvider dependencyProvider) {
    DependencyProvider.setInstance(dependencyProvider);
  }
}
