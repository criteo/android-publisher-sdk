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

package com.criteo.publisher.util

import com.criteo.publisher.mock.MockedDependenciesRule
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

class JsonSerializerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var serializer: JsonSerializer

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