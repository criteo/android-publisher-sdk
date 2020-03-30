package com.criteo.publisher.mock

import com.criteo.publisher.mock.DependenciesAnnotationInjection.InjectionException
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

    val dependencyProvider = mock<DummyDependencyProvider> {
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
    val dependencyProvider = mock<DummyTooManyDependencyProvider>()

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)

    assertThatCode {
      injection.process(dummyTest)
    }.isInstanceOf(InjectionException::class.java)
  }

  private fun Any.assertThatIsMock() {
    assertThat(mockingDetails(this).isMock).isTrue()
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

    lateinit var ignoredDependency: DummyDependency

    fun injectedDependency() = injectedDependency

  }

  class DummyTestWithNotProvidedDependency {

    @Inject
    lateinit var notProvidedDependency: NotProvidedDummyDependency

  }

  class NotProvidedDummyDependency
  open class DummyDependency(val mock: MockDummyDependency)
  open class SuperDummyDependency(val mock: MockDummyDependency)
  open class MockDummyDependency

  abstract class SuperDummyDependencyProvider {
    open fun provideSuperDummyDependency() = SuperDummyDependency(provideMockDummyDependency(1))
    open fun provideMockDummyDependency(ignored: Any?) = MockDummyDependency()
  }

  open class DummyDependencyProvider : SuperDummyDependencyProvider() {
    private fun ignoredDummyDependency() = DummyDependency(provideMockDummyDependency(2))
    open fun ignoredDummyDependency(ignored: Any) = DummyDependency(provideMockDummyDependency(3))
    open fun provideDummyDependency() = DummyDependency(provideMockDummyDependency(4))
  }

  abstract class DummyTooManyDependencyProvider : DummyDependencyProvider() {
    abstract fun provideDummyDependency2(): DummyDependency
  }

}