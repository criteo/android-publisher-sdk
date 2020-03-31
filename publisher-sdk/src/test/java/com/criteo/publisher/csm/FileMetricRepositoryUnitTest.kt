package com.criteo.publisher.csm

import com.criteo.publisher.Util.BuildConfigWrapper
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import java.io.File

class FileMetricRepositoryUnitTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Mock
  private lateinit var directory: MetricDirectory

  @InjectMocks
  private lateinit var repository: FileMetricRepository

  @Test
  fun getTotalSize_GivenSecurityException_SkipTheFile() {
    val throwingFile = mock<File> {
      on { length() } doThrow SecurityException::class
    }
    val goodFile = mock<File> {
      on { length() } doReturn 42
    }

    directory.stub {
      on { listFiles() } doReturn listOf(goodFile, throwingFile)
    }

    val totalSize = repository.totalSize

    assertThat(totalSize).isEqualTo(42)
  }

}