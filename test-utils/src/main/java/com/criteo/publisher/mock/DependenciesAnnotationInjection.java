package com.criteo.publisher.mock;

import android.annotation.SuppressLint;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

class DependenciesAnnotationInjection {

  @NonNull
  private final Object dependencyProvider;

  DependenciesAnnotationInjection(@NonNull Object dependencyProvider) {
    this.dependencyProvider = dependencyProvider;
  }

  /**
   * Inject dependency on fields annotated by the {@link javax.inject.Inject} annotation.
   * <p>
   * If a field is annotated, then this will try to find a provider method in the specified {@link
   * #dependencyProvider} that:
   * <ul>
   *   <li>is public</li>
   *   <li>takes no parameter</li>
   *   <li>have the same return type than the field</li>
   * </ul>
   * <p>
   * If no candidate, or multiple candidates are found, then an {@link InjectionException} is
   * thrown.
   *
   * @param testInstance instance of test class to process
   */
  void process(@NonNull Object testInstance) {
    processClassHierarchy(testInstance);
  }

  private void processClassHierarchy(@NonNull Object testInstance) {
    Class<?> testClass = testInstance.getClass();
    while (testClass != Object.class && testClass != null) {
      processClass(testInstance, testClass);
      testClass = testClass.getSuperclass();
    }
  }

  private void processClass(@NonNull Object testInstance, @NonNull Class<?> testClass) {
    Field[] fields = testClass.getDeclaredFields();
    for (Field field : fields) {
      if (field.getAnnotation(Inject.class) != null) {
        try {
          injectDependencyInto(field, testInstance);
        } catch (Exception e) {
          throw new InjectionException(e);
        }
      }
    }
  }

  private void injectDependencyInto(
      @NonNull Field field, @NonNull Object testInstance
  ) throws ReflectiveOperationException {
    Set<Method> methodCandidates = findMethodThatCanProvideDependency(field);
    if (methodCandidates.size() == 0) {
      throw InjectionException.noMethodCandidate(testInstance, field);
    } else if (methodCandidates.size() > 1) {
      throw InjectionException.tooManyMethodCandidates(testInstance, field, methodCandidates);
    }

    Method methodCandidate = methodCandidates.iterator().next();
    Object dependency = methodCandidate.invoke(dependencyProvider);

    field.setAccessible(true);
    field.set(testInstance, dependency);
    field.setAccessible(false);
  }

  private Set<Method> findMethodThatCanProvideDependency(Field field) {
    Set<Method> methodCandidates = new HashSet<>();
    Class<?> type = field.getType();
    Method[] methods = dependencyProvider.getClass().getMethods();

    for (Method method : methods) {
      if (getParameterCount(method) == 0) {
        Class<?> returnType = method.getReturnType();

        if (type.equals(returnType)) {
          methodCandidates.add(method);
        }
      }
    }

    return methodCandidates;
  }

  @SuppressLint("NewApi")
  private int getParameterCount(Method method) {
    if (canGetParameterCount()) {
      return method.getParameterCount();
    }
    return 0;
  }

  @SuppressLint("ObsoleteSdkInt")
  private static boolean canGetParameterCount() {
    // If we're not on Android, there is no API restrictions, so we bypass checks
    boolean isNotRunningOnAndroidEmulator = VERSION.SDK_INT == 0;
    return isNotRunningOnAndroidEmulator || VERSION.SDK_INT >= VERSION_CODES.O;
  }

  static final class InjectionException extends RuntimeException {

    InjectionException(Exception e) {
      super(e);
    }

    InjectionException(String msg) {
      super(msg);
    }

    static InjectionException noMethodCandidate(
        @NonNull Object testInstance,
        @NonNull Field field
    ) {
      return new InjectionException(
          "No method found to provide dependency for " + field + " in " + testInstance);
    }

    static InjectionException tooManyMethodCandidates(
        @NonNull Object testInstance,
        @NonNull Field field,
        @NonNull Set<Method> methodCandidates
    ) {
      String msg =
          "Too many methods found to provide dependency for " + field + " in " + testInstance
              + ". Candidates are: " + methodCandidates;
      if (!canGetParameterCount()) {
        msg = msg + ". You're running your tests on an old device. "
            + "This injection engine cannot count parameters of method candidates.";
      }
      return new InjectionException(msg);
    }
  }

}
