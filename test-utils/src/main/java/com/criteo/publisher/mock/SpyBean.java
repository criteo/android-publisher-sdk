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
