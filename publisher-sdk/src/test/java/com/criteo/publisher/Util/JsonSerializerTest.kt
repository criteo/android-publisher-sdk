package com.criteo.publisher.Util

import com.criteo.publisher.mock.MockedDependenciesRule
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.io.OutputStream

class JsonSerializerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  private lateinit var serializer: JsonSerializer

  @Before
  fun setUp() {
    serializer = mockedDependenciesRule.dependencyProvider.provideJsonSerializer()
  }

  @Test
  fun write_GivenStreamThatThrowsWhenWriting_ThrowIoException() {
    val value = Dummy()
    val stream = mock<OutputStream> {
      on { write(any<Int>()) } doThrow(IOException::class)
      on { write(any<ByteArray>()) } doThrow(IOException::class)
      on { write(any(), any(), any()) } doThrow(IOException::class)
      on { flush() } doThrow(IOException::class)
    }

    assertThatCode {
      serializer.write(value, stream)
    }.isInstanceOf(IOException::class.java)
  }

  @Test
  fun write_GivenStream_FlushIt() {
    val value = Dummy()
    val stream = mock<OutputStream>()

    serializer.write(value, stream)

    verify(stream).flush()
  }

  @Test
  fun write_GivenStream_DoNotCloseIt() {
    val value = Dummy()
    val stream = mock<OutputStream>()

    serializer.write(value, stream)

    verify(stream, never()).close()
  }

  private data class Dummy(private val dummy: String = "")

}