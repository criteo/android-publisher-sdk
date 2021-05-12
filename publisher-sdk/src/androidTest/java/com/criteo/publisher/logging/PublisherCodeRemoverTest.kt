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

package com.criteo.publisher.logging

import com.criteo.publisher.util.printStacktraceToString
import com.dummypublisher.DummyPublisherCode
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class PublisherCodeRemoverTest {

  private lateinit var remover: PublisherCodeRemover

  @Before
  fun setUp() {
    remover = PublisherCodeRemover()
  }

  @Test
  fun removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowedCode_ReturnEqualException() {
    // Stack is composed of few JDK for threading + this test
    val newThreadException = Executors.newSingleThreadExecutor().submit(Callable<Exception> {
      Exception()
    }).get()
    removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowed_ReturnEqualException(newThreadException)

    // Stack is composed of JUnit framework + this test
    val testException = Exception()
    removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowed_ReturnEqualException(testException)

    // Stack is composed of JUnit framework + this test + JDK + this test
    val insideJdkException = exceptionThrownBy {
      mutableMapOf<Any, Any>().computeIfAbsent("any") {
        @Suppress("TooGenericExceptionThrown")
        throw RuntimeException()
      }
    }
    removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowed_ReturnEqualException(insideJdkException)

    val recursiveException2 = Exception()
    val recursiveException = Exception(recursiveException2)
    recursiveException2.initCause(recursiveException)
    removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowed_ReturnEqualException(recursiveException)

    // addSuppressed is not used because it is removed by D8. See PublisherCodeRemoverJvmTest
    val composedException = Exception(newThreadException)
    newThreadException.initCause(testException)
    testException.initCause(insideJdkException)
    insideJdkException.initCause(recursiveException)

    removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowed_ReturnEqualException(composedException)
  }

  private fun removePublisherCode_GivenExceptionOnlyInsideSdkCodeOrAllowed_ReturnEqualException(original: Throwable) {
    val originalStacktrace = original.printStacktraceToString()

    val cleaned = removePublisherCode(original)

    assertThat(cleaned.printStacktraceToString()).isEqualTo(originalStacktrace)
  }

  @Test
  fun removePublisherCode_GivenExceptionThrownByPublisherCode_HidePublisherCode() {
    val publisherCode = DummyPublisherCode.sdkDummyInterfaceThrowingGenericException()

    // Stack is: JUnit + this test (intercept exception) + publisher code (cause)
    val exception = exceptionThrownBy {
      try {
        publisherCode.foo()
      } catch (e: Exception) {
        throw SdkException("sdk exception", e)
      }
    }

    // Expect:
    // - cause exception is changed to a publisher exception, stacktrace is cleaned
    // - intercept exception is left unchanged
    val cleaned = removePublisherCode(exception)

    assertThat(cleaned)
        .hasNoSuppressedExceptions()
        .hasMessage("sdk exception")
        .isInstanceOf(SdkException::class.java)
        .hasMessageNotContaining(DummyPublisherCode.secrets)
        .satisfies {
          assertThat(it.stackTrace).isEqualTo(exception.stackTrace)
        }

    assertThat(cleaned.cause)
        .isInstanceOf(PublisherCodeRemover.PublisherException::class.java)
        .hasMessageNotContaining(DummyPublisherCode.secrets)
        .hasStackTraceContainingOnlyAllowedStack()
  }

  @Test
  fun removePublisherCode_GivenSdkExceptionThrownBySdkCodeThroughPublisherCode_OnlyHidePublisherCode() {
    val publisherCode = DummyPublisherCode.sdkDummyInterfaceCallingSdkCode {
      addDummyStacks {
        throw IllegalStateException("sdk cause")
      }
    }

    // Stack is: JUnit + this test (intercept exception) + publisher code + SDK code (cause)
    val exception = exceptionThrownBy {
      try {
        publisherCode.foo()
      } catch (e: Exception) {
        throw SdkException("sdk exception", e)
      }
    }

    // Expect:
    // - cause exception remain but stacktrace is cleaned
    // - intercept exception is left unchanged
    val cleaned = removePublisherCode(exception)

    assertThat(cleaned)
        .hasNoSuppressedExceptions()
        .hasMessage("sdk exception")
        .isInstanceOf(SdkException::class.java)
        .hasMessageNotContaining(DummyPublisherCode.secrets)
        .satisfies {
          assertThat(it.stackTrace).isEqualTo(exception.stackTrace)
        }

    assertThat(cleaned.cause)
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("sdk cause")
        .hasStackTraceContainingOnlyAllowedStack()
  }

  @Test
  fun removePublisherCode_GivenSdkExceptionWithMessageOfGenericPublisherException_RemovePublisherMessageInSdkExceptionButKeepInfoAboutGenericException() {
    val publisherCode = DummyPublisherCode.sdkDummyInterfaceThrowingGenericException()

    // Stack is: JUnit + this test (intercept exception) + publisher code (cause)
    val exception = exceptionThrownBy {
      try {
        publisherCode.foo()
      } catch (e: Exception) {
        // JDK copy the publisher's message into the SDK exception.
        // This demonstrates that remover works recursively and works on exception caused by publishers but also on
        // exception caused by the SDK (which is caused by publishers), ...:
        // - let's say the message of e is foo and e is of type FooException
        // - SdkException(e).message is then FooException: foo
        // - SdkException(SdkException(e)).message is then SdkException: FooException: foo
        // - SdkException(SdkException(SdkException(e))).message is then SdkException: SdkException: FooException: foo
        //
        // Expected messages are, respectively (ignoring the packages):
        // - An exception occurred from publisher's code (with exception of type PublisherException)
        // - PublisherException: An exception occurred from publisher's code
        // - SdkException: PublisherException: An exception occurred from publisher's code
        // - SdkException: SdkException: PublisherException: An exception occurred from publisher's code
        throw SdkException(SdkException(SdkException(e)))
      }
    }

    // Expect:
    // - cause exception is changed to a publisher exception, stacktrace is cleaned
    // - sdk exceptions have message changed
    val cleaned = removePublisherCode(exception)

    val packagePattern = "[${'$'}a-zA-Z.]+"
    assertThat(cleaned.message).matches("${packagePattern}SdkException: " +
        "${packagePattern}SdkException: " +
        "${packagePattern}PublisherException: A IllegalArgumentException exception occurred from publisher's code")
    assertThat(cleaned.printStacktraceToString())
        .doesNotContain(DummyPublisherCode.secrets)
        .doesNotContain("com.dummypublisher")
  }

  @Test
  fun removePublisherCode_GivenSdkExceptionWithMessageOfCustomPublisherException_RemovePublisherMessageInSdkException() {
    // Stack is: JUnit + this test (intercept exception) + publisher code (cause)
    val exception = exceptionThrownBy {
      DummyPublisherCode.sdkDummyInterfaceThrowingCustomException().foo()
    }

    // Expect:
    // - cause exception is changed to a publisher exception, stacktrace is cleaned
    // - sdk exceptions have message changed
    val cleaned = removePublisherCode(exception)

    assertThat(cleaned.message).isEqualTo("A custom exception occurred from publisher's code")
    assertThat(cleaned.printStacktraceToString())
        .doesNotContain(DummyPublisherCode.secrets)
        .doesNotContain("com.dummypublisher")
  }

  @Test
  fun removePublisherCode_GivenExceptionDuringOperation_ReturnMarkerException() {
    val keptStack = StackTraceElement("keep", "this", null, -1)
    val exception = spy(IllegalStateException("message of the exception")) {
      on { stackTrace } doReturn arrayOf(
          keptStack,
          keptStack,
          keptStack,
          keptStack,
          keptStack,
          StackTraceElement("ignored", "this", null, -1),
          StackTraceElement("ignored", "this", null, -1)
      )
    }

    remover = spy(remover)
    doThrow(exception).whenever(remover).removePublisherCodeDeeply(any(), any())

    val cleaned = removePublisherCode(Exception())

    assertThat(cleaned)
        .hasMessageContaining("IllegalStateException")
        .hasMessageContaining("message of the exception")
        .hasNoCause()
        .hasNoSuppressedExceptions()

    assertThat(cleaned.stackTrace).hasSize(5)
    assertThat(cleaned.stackTrace.toSet()).containsOnly(keptStack)
  }

  private fun exceptionThrownBy(action: () -> Unit): Throwable {
    try {
      action()
    } catch (t: Throwable) {
      return t
    }

    throw AssertionError("No exception was thrown")
  }

  private fun AbstractThrowableAssert<*, *>.hasStackTraceContainingOnlyAllowedStack(): AbstractThrowableAssert<*, *> {
    return hasStackTraceContaining("com.criteo.")
        .hasStackTraceContaining("java.")
        .hasStackTraceContaining("org.junit.")
        .satisfies {
          assertThat(it.stackTrace).allSatisfy {
            assertThat(it.className).doesNotStartWith("com.dummypublisher")
          }
        }
  }

  private fun addDummyStacks(nbStack: Int = 5, thenAction: () -> Unit) {
    if (nbStack <= 1) {
      thenAction()
    } else {
      addDummyStacks(nbStack - 1, thenAction)
    }
  }

  private fun removePublisherCode(throwable: Throwable): Throwable {
    // Copy the throwable to simplify assertions because remover mutate its input.
    return remover.removePublisherCode(throwable.deepCopy())
  }

  private fun Throwable.deepCopy(): Throwable {
    val bytes = ByteArrayOutputStream().use { baos ->
      ObjectOutputStream(baos).use { oos ->
        oos.writeObject(this)
      }

      baos.toByteArray()
    }

    return ByteArrayInputStream(bytes).use { bais ->
      ObjectInputStream(bais).use { ois ->
        ois.readObject()
      }
    } as Throwable
  }

  interface SdkDummyInterface {
    fun foo()
  }

  class SdkException : RuntimeException {
    constructor(message: String, throwable: Throwable) : super(message, throwable)
    constructor(throwable: Throwable) : super(throwable)
  }
}
