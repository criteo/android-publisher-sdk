package com.criteo.publisher.mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import android.annotation.SuppressLint;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;

class DependenciesAnnotationInjection {

  @NonNull
  private final Object dependencyProvider;

  DependenciesAnnotationInjection(@NonNull Object dependencyProvider) {
    this.dependencyProvider = dependencyProvider;
  }

  /**
   * Inject dependency on fields annotated by the {@link Inject} or {@link MockBean} annotation.
   * <p>
   * If a field is annotated with @{@link Inject}, then this will try to find a provider method in
   * the specified {@link #dependencyProvider}. The found provider method is invoked and its result
   * is injected into the field. Provider methods are found with those criterias:
   * <ul>
   *   <li>is public</li>
   *   <li>takes no parameter</li>
   *   <li>have the same return type than the field</li>
   * </ul>
   * <p>
   * If a field is annotated with {@link MockBean}, then it will inject a new mock instance into
   * the field. Moreover, this will find a provider method in the dependency provider and will make
   * it return the injected mock. The given dependency provider should then be a mock or a spy.
   * The criterias to find the provider method are:
   * <ul>
   *   <li>is public</li>
   *   <li>have the same return type than the field</li>
   * </ul>
   * <p>
   * If no candidate, or multiple candidates are found, then an {@link InjectionException} is
   * thrown.
   *
   * @param testInstance instance of test class to process
   */
  void process(@NonNull Object testInstance) {
    try {
      injectMocks(testInstance);
      injectDependencies(testInstance);
    } catch (Exception e) {
      throw new InjectionException(e);
    }
  }

  private void injectMocks(@NonNull Object testInstance) throws ReflectiveOperationException {
    Collection<Field> fields = getAllAnnotatedFields(testInstance, MockBean.class);
    for (Field field : fields) {
      injectMocksInto(testInstance, field);
    }
  }

  private void injectMocksInto(
      @NonNull Object testInstance,
      @NonNull Field field
  ) throws ReflectiveOperationException {
    checkDependencyProviderIsAMock();

    Method providerMethod = findProviderMethod(testInstance, field, true);
    Object dependency = mock(field.getType());
    setFieldValue(testInstance, field, dependency);
    stubDependencyProvider(providerMethod, dependency);
  }

  private void checkDependencyProviderIsAMock() {
    MockingDetails mockingDetails = mockingDetails(dependencyProvider);
    if (!mockingDetails.isMock() && !mockingDetails.isSpy()) {
      throw new IllegalArgumentException();
    }
  }

  private void stubDependencyProvider(
      @NonNull Method providerMethod,
      @NonNull Object dependency
  ) throws ReflectiveOperationException {
    // Execute a doReturn(dependency).when(dependencyProvider).invokeMethod(with enough any())
    Object stubbing = doReturn(dependency).when(dependencyProvider);

    int parameterCount = getParameterCount(providerMethod);
    try {
      Object[] args = new Object[parameterCount];
      for (int i = 0; i < parameterCount; i++) {
        args[i] = Mockito.any();
      }

      providerMethod.invoke(stubbing, args);
    } catch (MockitoException e) {
      if (!canGetParameterCount()) {
        throw new MockitoException("You're running your test on an old device. "
            + "This injection engine cannot count parameters of method candidates. "
            + "And so, it can not mock provider methods taking parameters.", e);
      } else {
        throw e;
      }
    }
  }

  private void injectDependencies(@NonNull Object testInstance) throws ReflectiveOperationException {
    Collection<Field> fields = getAllAnnotatedFields(testInstance, Inject.class);
    for (Field field : fields) {
      injectDependencyInto(field, testInstance);
    }
  }

  private void injectDependencyInto(
      @NonNull Field field, @NonNull Object testInstance
  ) throws ReflectiveOperationException {
    Method providerMethod = findProviderMethod(testInstance, field, false);
    Object dependency = providerMethod.invoke(dependencyProvider);

    setFieldValue(testInstance, field, dependency);
  }

  private Collection<Field> getAllAnnotatedFields(
      @NonNull Object instance,
      @NonNull Class<? extends Annotation> annotation) {
    List<Field> annotatedFields = new ArrayList<>();
    Class<?> klass = instance.getClass();
    while (klass != Object.class && klass != null) {
      Field[] fields = klass.getDeclaredFields();

      for (Field field : fields) {
        if (field.getAnnotation(annotation) != null) {
          annotatedFields.add(field);
        }
      }

      klass = klass.getSuperclass();
    }

    return annotatedFields;
  }

  private Method findProviderMethod(
      @NonNull Object testInstance,
      @NonNull Field field,
      boolean acceptParameters
  ) {
    Set<Method> methodCandidates = findMethodThatCanProvideDependency(field, acceptParameters);
    if (methodCandidates.size() == 0) {
      throw InjectionException.noMethodCandidate(testInstance, field);
    } else if (methodCandidates.size() > 1) {
      throw InjectionException.tooManyMethodCandidates(testInstance, field, methodCandidates);
    }

    return methodCandidates.iterator().next();
  }

  private Set<Method> findMethodThatCanProvideDependency(@NonNull Field field,  boolean acceptParameters) {
    Set<Method> methodCandidates = new HashSet<>();
    Class<?> type = field.getType();
    Method[] methods = dependencyProvider.getClass().getMethods();

    for (Method method : methods) {
      if (acceptParameters || getParameterCount(method) == 0) {
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

  private void setFieldValue(@NonNull Object testInstance,
      @NonNull Field field, Object value) throws IllegalAccessException {
    field.setAccessible(true);
    field.set(testInstance, value);
    field.setAccessible(false);
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
