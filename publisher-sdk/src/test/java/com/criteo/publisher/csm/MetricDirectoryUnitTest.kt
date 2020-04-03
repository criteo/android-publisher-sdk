package com.criteo.publisher.csm

import android.content.Context
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import java.io.File
import java.io.FilenameFilter

class MetricDirectoryUnitTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var parser: MetricParser

  @InjectMocks
  private lateinit var directory: MetricDirectory

  @Test
  fun listFiles_GivenDirectoryReturningNull_ReturnEmpty() {
    directory = spy(directory) {
      doReturn(mock<File>()).whenever(mock).directoryFile
    }

    directory.directoryFile.stub {
      on { listFiles(any<FilenameFilter>()) } doReturn null
    }

    val files = directory.listFiles()

    assertThat(files).isEmpty()
  }

}