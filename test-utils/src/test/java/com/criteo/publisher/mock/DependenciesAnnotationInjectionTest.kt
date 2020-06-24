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

import com.criteo.publisher.mock.DependenciesAnnotationInjection.InjectionException
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import org.mockito.Mockito.mockingDetails
import javax.inject.Inject

class DependenciesAnnotationInjectionTest {

  @Test
  fun processSpy_GivenATestInstanceWithSpy_InjectSpyInDependencyProvider() {
    val dependencyProvider = spy(DummyDependencyProvider())

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)
    injection.process(dummyTest)

    dummyTest.spyDependency.assertThatIsSpy()
    dummyTest.spyDependency.transitiveSpy.assertThatIsSpy()

    assertThat(dummyTest.spyDependency).isSameAs(dummyTest.injectedDependency().spy)
    assertThat(dummyTest.spyDependency).isSameAs(dummyTest.superDependency.spy)
    assertThat(dummyTest.spyDependency).isSameAs(dependencyProvider.provideSpyDummyDependency())
    assertThat(dummyTest.spyDependency.transitiveSpy).isSameAs(dummyTest.transitiveSpy)
    assertThat(dummyTest.spyDependency.transitiveSpy).isSameAs(dependencyProvider.provideTransitiveSpyDummyDependency())
  }

  @Test
  fun processMock_GivenATestInstanceWithMock_InjectMockInDependencyProvider() {
    val dependencyProvider = spy(DummyDependencyProvider())

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)
    injection.process(dummyTest)

    dummyTest.mockDependency.assertThatIsMock()
    assertThat(dummyTest.mockDependency).isSameAs(dummyTest.injectedDependency().mock)
    assertThat(dummyTest.mockDependency).isSameAs(dummyTest.superDependency.mock)
    assertThat(dummyTest.mockDependency).isSameAs(dependencyProvider.provideMockDummyDependency(null))
  }

  @Test
  fun processInject_GivenATestInstanceWithADependencyProvider_InjectDependencies() {
    val dummyDependency = mock<DummyDependency>()
    val superDummyDependency = mock<SuperDummyDependency>()

    val dependencyProvider = spy(DummyDependencyProvider()) {
      on { provideDummyDependency() } doReturn dummyDependency
      on { provideSuperDummyDependency() } doReturn superDummyDependency
    }

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)
    injection.process(dummyTest)

    assertThat(dummyTest.injectedDependency()).isEqualTo(dummyDependency)
    assertThat(dummyTest.superDependency).isEqualTo(dummyDependency)
    assertThat(dummyTest.injectedSuperDependency).isEqualTo(superDummyDependency)
    assertThatCode {
      dummyTest.ignoredDependency
    }.isInstanceOf(UninitializedPropertyAccessException::class.java)
  }

  @Test
  fun processInject_GivenADependencyProviderWithSpyAndMock_InjectDependencies() {
    val mockDummyDependency = mock<MockDummyDependency>()
    val spyDummyDependency = mock<SpyDummyDependency>()

    val dependencyProvider = spy(DummyDependencyProvider()) {
      on { provideMockDummyDependency(anyOrNull()) } doReturn mockDummyDependency
      on { provideSpyDummyDependency() } doReturn spyDummyDependency
    }

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)
    injection.process(dummyTest)

    assertThat(dummyTest.spyDependency).isSameAs(spyDummyDependency)
    assertThat(dummyTest.mockDependency).isSameAs(mockDummyDependency)
    assertThat(dummyTest.injectedDependency().mock).isSameAs(mockDummyDependency)
    assertThat(dummyTest.superDependency.mock).isSameAs(mockDummyDependency)
  }

  @Test
  fun processInject_GivenFieldWithoutProvidedDependency_ThrowException() {
    val dependencyProvider = mock<DummyDependencyProvider>()

    val dummyTest = DummyTestWithNotProvidedDependency()

    val injection = DependenciesAnnotationInjection(dependencyProvider)

    assertThatCode {
      injection.process(dummyTest)
    }.isInstanceOf(InjectionException::class.java)
  }

  @Test
  fun processInject_GivenFieldWithTooManyProvidedDependency_ThrowException() {
    val dependencyProvider = DummyTooManyDependencyProvider()

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)

    assertThatCode {
      injection.process(dummyTest)
    }.isInstanceOf(InjectionException::class.java)
  }

  private fun Any.assertThatIsMock() {
    assertThat(mockingDetails(this).isMock).isTrue()
  }

  private fun Any.assertThatIsSpy() {
    assertThat(mockingDetails(this).isSpy).isTrue()
  }

  open class SuperDummyTest {
    @Inject
    internal lateinit var superDependency: DummyDependency
  }

  class DummyTest : SuperDummyTest() {

    @Inject
    private lateinit var injectedDependency: DummyDependency

    @Inject
    internal lateinit var injectedSuperDependency: SuperDummyDependency

    @MockBean
    lateinit var mockDependency: MockDummyDependency

    @SpyBean
    lateinit var spyDependency: SpyDummyDependency

    @SpyBean
    lateinit var transitiveSpy: TransitiveSpyDummyDependency

    lateinit var ignoredDependency: DummyDependency

    fun injectedDependency() = injectedDependency

  }

  class DummyTestWithNotProvidedDependency {

    @Inject
    lateinit var notProvidedDependency: NotProvidedDummyDependency

  }

  class NotProvidedDummyDependency
  open class DummyDependency(val mock: MockDummyDependency, val spy: SpyDummyDependency)
  open class SuperDummyDependency(val mock: MockDummyDependency, val spy: SpyDummyDependency)
  open class MockDummyDependency
  open class SpyDummyDependency(val transitiveSpy: TransitiveSpyDummyDependency)
  open class TransitiveSpyDummyDependency

  abstract class SuperDummyDependencyProvider {
    open fun provideSuperDummyDependency() = SuperDummyDependency(
        provideMockDummyDependency(1),
        provideSpyDummyDependency())

    open fun provideMockDummyDependency(ignored: Any?) = MockDummyDependency()
    open fun provideSpyDummyDependency(): SpyDummyDependency {
      return SpyDummyDependency(provideTransitiveSpyDummyDependency())
    }
    open fun provideTransitiveSpyDummyDependency() = TransitiveSpyDummyDependency()
  }

  open class DummyDependencyProvider : SuperDummyDependencyProvider() {
    private fun ignoredDummyDependency() = DummyDependency(
        provideMockDummyDependency(2),
        provideSpyDummyDependency())

    open fun ignoredDummyDependency(ignored: Any) = DummyDependency(
        provideMockDummyDependency(3),
        provideSpyDummyDependency())

    open fun provideDummyDependency() = DummyDependency(
        provideMockDummyDependency(4),
        provideSpyDummyDependency())
  }

  open class DummyTooManyDependencyProvider : DummyDependencyProvider() {
    open fun provideDummyDependency2() = DummyDependency(
        provideMockDummyDependency(5),
        provideSpyDummyDependency())
  }

}