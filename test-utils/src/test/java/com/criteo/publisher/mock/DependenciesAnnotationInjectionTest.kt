package com.criteo.publisher.mock

import com.criteo.publisher.mock.DependenciesAnnotationInjection.InjectionException
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import javax.inject.Inject

class DependenciesAnnotationInjectionTest {

  @Test
  fun process_GivenATestInstanceWithADependencyProvider_InjectDependencies() {
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
  fun process_GivenFieldWithoutProvidedDependency_ThrowException() {
    val dependencyProvider = mock<DummyDependencyProvider>()

    val dummyTest = DummyTestWithNotProvidedDependency()

    val injection = DependenciesAnnotationInjection(dependencyProvider)

    assertThatCode {
      injection.process(dummyTest)
    }.isInstanceOf(InjectionException::class.java)
  }

  @Test
  fun process_GivenFieldWithTooManyProvidedDependency_ThrowException() {
    val dependencyProvider = mock<DummyTooManyDependencyProvider>()

    val dummyTest = DummyTest()

    val injection = DependenciesAnnotationInjection(dependencyProvider)

    assertThatCode {
      injection.process(dummyTest)
    }.isInstanceOf(InjectionException::class.java)
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

    lateinit var ignoredDependency: DummyDependency

    fun injectedDependency() = injectedDependency

  }

  class DummyTestWithNotProvidedDependency {

    @Inject
    lateinit var notProvidedDependency: NotProvidedDummyDependency

  }

  class NotProvidedDummyDependency
  open class DummyDependency
  open class SuperDummyDependency

  abstract class SuperDummyDependencyProvider {
    abstract fun provideSuperDummyDependency(): SuperDummyDependency
  }

  abstract class DummyDependencyProvider : SuperDummyDependencyProvider() {
    private fun ignoredDummyDependency(): DummyDependency = DummyDependency()
    abstract fun ignoredDummyDependency(ignored: Any): DummyDependency
    abstract fun provideDummyDependency(): DummyDependency
  }

  abstract class DummyTooManyDependencyProvider : DummyDependencyProvider() {
    abstract fun provideDummyDependency2(): DummyDependency
  }

}