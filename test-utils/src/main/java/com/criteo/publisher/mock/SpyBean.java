package com.criteo.publisher.mock;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identify dependencies in the DI engine that should be fed by spies.
 * <p>
 * The annotated fields are injected before each test methods by a spy on provided dependencies.
 * This is automatically enabled when using the {@link MockedDependenciesRule} rule.
 * <p>
 * This reduces required boilerplate. With boilerplate explicitly written:
 * <pre><code>
 *   public class MyAwesomeIntegrationTest {
 *
 *     {@literal @}Rule
 *     public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();
 *
 *     MySuperBean bean;
 *
 *     {@literal @}Before
 *     public void setup() {
 *         MockitoAnnotations.initMocks(this);
 *
 *         bean = spy(mockedDependenciesRule.getDependencyProvider().provideMySuperBean());
 *         when(mockedDependenciesRule.getDependencyProvider().provideMySuperBean())
 *             .thenReturn(bean);
 *     }
 * </code></pre>
 * <p>
 * Without the boilerplate by using this annotation:
 * <pre><code>
 *   public class MyAwesomeIntegrationTest {
 *
 *     {@literal @}Rule
 *     public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();
 *
 *     {@literal @}SpyBean
 *     MySuperBean bean;
 * </code></pre>
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface SpyBean {

}
