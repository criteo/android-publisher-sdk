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
package com.criteo.publisher.mock

import com.nhaarman.mockitokotlin2.spy
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test

class DependenciesAnnotationInjectionAndroidTest {

  /**
   * When a lambda is capturing only `this`, then a new method is created:
   * - method is synthetic
   * - method has no argument
   * - method returns what the lambda returns
   *
   * For [javax.inject.Inject] objects, the [DependenciesAnnotationInjection] is looking for methods without argument
   * and returning the expected type. So the generated lambda method is a matching candidate.
   * Unless the injection check candidate methods are not synthetic.
   *
   * Note that this is only observable on Android: the Android SDK is generating such method during compilation while
   * the JDK is only placing an invoke dynamic at call-site which will lazily generate and call a SAM during runtime
   * (via its [java.lang.invoke.LambdaMetafactory]).
   * The difference is that JDK has no effect on callers while Android has.
   */
  @Test
  fun processInject_GivenJavaDependencyProviderWithLambdaCapturingThis_DoNotThrow() {
    val dependencyProvider = spy(JavaDummyDependencyProvider())
    val dummyTest = JavaDummyDependencyProvider.JavaDummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)

    assertThatCode {
      injection.process(dummyTest)
    }.doesNotThrowAnyException()
  }
}
