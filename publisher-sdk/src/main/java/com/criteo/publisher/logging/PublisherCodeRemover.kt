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

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.annotation.OpenForTesting
import java.lang.reflect.Field
import kotlin.math.min

@OpenForTesting
internal class PublisherCodeRemover {

  private val criteoPackagePrefix = "com.criteo."

  // Prefixes of package frameworks that are allowed when traversing the stacktrace.
  // These packages are not hidden because they provide useful context about the exception.
  private val allowedFrameworkPackagePrefixes = listOf(
      // Java: indicate if the exception happens in a worker thread pool, or from standard object
      "java.",
      "javax.",
      "sun.",
      "com.sun.",

      // Kotlin: same as Java
      "com.intellij.",
      "org.jetbrains.",
      "kotlin.",

      // Android: indicate from where the stacktrace comes from: Activity, Service, callback, ...
      "android.",
      "com.android.",
      "androidx.",
      "dalvik.",
      "libcore.",

      // Mediation Libs: The SDK is coupled to these libraries through the mediation adapters.
      // This will indicate if an exception happens during mediation.
      "com.google",

      // Libs used by the SDK: exception might come from them
      "org.json",
      "com.squareup.",

      // Testing libs
      "org.junit."
  )

  private val privateStackTraceElement = StackTraceElement("<private class>", "<private method>", null, 0)

  /**
   * Remove, from the [throwable] stacktrace, all the code coming from the publisher.
   *
   * @param throwable [Throwable] that will be mutated when removing publisher code from it.
   */
  @Suppress("TooGenericExceptionCaught")
  fun removePublisherCode(throwable: Throwable): Throwable {
    return try {
      removePublisherCodeDeeply(throwable, mutableMapOf())
    } catch (e: Throwable) {
      PublisherCodeRemoverException(e)
    }
  }

  @VisibleForTesting
  internal fun removePublisherCodeDeeply(
      original: Throwable,
      visited: MutableMap<Throwable, Throwable>
  ): Throwable {
    // Handle circular reference of throwable. For instance, throwable1 is caused by throwable2 and vice versa
    visited[original]?.let {
      return it
    }

    val cleanedException = if (original.mightBeThrownByPublisher()) {
      // Hide exceptions coming from publisher. So they are removed.
      if (original.isAllowedFramework()) {
        // Exception class name is known but message might contain sensitive/private information.
        PublisherException(original)
      } else {
        // Exception message or class name might contain sensitive/private information.
        PublisherException()
      }
    } else {
      original
    }

    visited[original] = cleanedException

    val isMessageDerivedFromCause = original.cause?.let { it.toString() == original.message } ?: false

    removePublisherCodeFromCause(original, cleanedException, visited)
    removePublisherCodeFromSuppressedExceptions(original, cleanedException, visited)
    removePublisherCodeFromStacktrace(original, cleanedException)

    cleanedException.cause?.let {
      if (isMessageDerivedFromCause) {
        // Reset message from new cause as it can have been changed meanwhile
        with(ThrowableInternal) {
          cleanedException.internalDetailMessage = it.toString()
        }
      }
    }

    return cleanedException
  }

  private fun removePublisherCodeFromCause(
      original: Throwable,
      cleanedException: Throwable,
      visited: MutableMap<Throwable, Throwable>
  ) {
    original.cause?.let {
      with(ThrowableInternal) {
        // Use internal because [Throwable#initCause] throws when caused is already set
        cleanedException.internalCause = removePublisherCodeDeeply(it, visited)
      }
    }
  }

  @SuppressLint("NewApi") // Ok, minSdkLevel of the real SDK is 19, see DeviceUtil#isVersionSupported
  private fun removePublisherCodeFromSuppressedExceptions(
      original: Throwable,
      cleanedException: Throwable,
      visited: MutableMap<Throwable, Throwable>
  ) {
    val originalSuppressed = original.suppressed
    if (originalSuppressed.isNotEmpty()) {
      // Don't do anything when there is no suppressed exceptions: either it's really empty and there is nothing to do,
      // either suppressed exception feature is deactivated on this throwable and we should not change the field value.

      val cleanedSuppressed = originalSuppressed.map {
        removePublisherCodeDeeply(it, visited)
      }

      with(ThrowableInternal) {
        cleanedException.internalSuppressedExceptions = cleanedSuppressed
      }
    }
  }

  private fun removePublisherCodeFromStacktrace(original: Throwable, cleanedException: Throwable) {
    val newStackTrace = mutableListOf<StackTraceElement>()
    original.stackTrace.forEach {
      if (it.isSdk() || it.isAllowedFramework()) {
        newStackTrace.add(it)
      } else if (newStackTrace.isEmpty() || newStackTrace.last() != privateStackTraceElement) {
        // Group the private stacks together
        newStackTrace.add(privateStackTraceElement)
      }
    }

    cleanedException.stackTrace = newStackTrace.toTypedArray()
  }

  private fun StackTraceElement.isAllowedFramework(): Boolean {
    return allowedFrameworkPackagePrefixes.any { className.startsWith(it) }
  }

  private fun StackTraceElement.isSdk(): Boolean {
    return className.startsWith(criteoPackagePrefix)
  }

  private fun Throwable.mightBeThrownByPublisher(): Boolean {
    val firstNotFrameworkStackTraceElement = stackTrace.firstOrNull { !it.isAllowedFramework() }
        ?: return false // Exception is thrown by neither the SDK nor the publisher: it can be JDK, or Android SDK.

    // Exception can belong to: the SDK, the publisher, a not listed third-party. For the last case, we prefer to
    // consider that it belongs to the publisher.
    return !firstNotFrameworkStackTraceElement.isSdk()
  }

  private fun Throwable.isAllowedFramework(): Boolean {
    return allowedFrameworkPackagePrefixes.any { javaClass.name.startsWith(it) }
  }

  object ThrowableInternal {

    private val causeField = SafeField("cause")
    private val suppressedField = SafeField("suppressedExceptions")
    private val detailMessageField = SafeField("detailMessage")

    var Throwable.internalCause: Throwable?
      get() = causeField.get(this) as Throwable?
      set(value) = causeField.set(this, value)

    @Suppress("UNCHECKED_CAST")
    var Throwable.internalSuppressedExceptions: List<Throwable>?
      get() = suppressedField.get(this) as List<Throwable>?
      set(value) = suppressedField.set(this, value)

    var Throwable.internalDetailMessage: String?
      get() = detailMessageField.get(this) as String?
      set(value) = detailMessageField.set(this, value)

    // Accessing fields that are missing from the classpath can throw ReflectiveOperationException or LinkageError or
    // other throwable depending of the JVM. In this case it is safer to just catch everything.
    @Suppress("TooGenericExceptionCaught")
    private class SafeField(private val name: String) {

      private val field: Field?

      init {
        this.field = try {
          val field = java.lang.Throwable::class.java.getDeclaredField(name)
          field.isAccessible = true
          field
        } catch (e: Throwable) {
          log("Field `$name` not present in Throwable class", e)
          null
        }
      }

      fun get(throwable: Throwable): Any? {
        return try {
          field?.get(throwable)
        } catch (e: Throwable) {
          log("Impossible to read from field `$name`", e)
          null
        }
      }

      fun set(throwable: Throwable, value: Any?) {
        try {
          field?.set(throwable, value)
        } catch (e: Throwable) {
          log("Impossible to set field `$name`", e)
        }
      }

      private fun log(message: String, throwable: Throwable) {
        // Use Android logger directly because this class is used while handling remote log.
        // We should not take the risk to generate logs while handling some.
        // It could generate an infinite loop.
        Log.d(LogTag.with("ThrowableInternal"), message, throwable)
      }
    }
  }

  class PublisherException : RuntimeException {
    constructor() : this("custom")
    constructor(throwable: Throwable) : this(throwable.javaClass.simpleName)
    private constructor(exceptionName: String) : super("A $exceptionName exception occurred from publisher's code")
  }

  /**
   * Exception thrown when removing publisher code from a throwable.
   *
   * By construction, this exception does not contains publisher code so it is safe to send it via the remote logger.
   * Only few stacks are kept to give a hint on the context and to not display any publisher code.
   */
  private class PublisherCodeRemoverException(cause: Throwable) : RuntimeException(
      "Exception occurred while removing publisher code. ${cause.javaClass.simpleName}: ${cause.message}"
  ) {
    init {
      stackTrace = cause.stackTrace.copyOf(min(cause.stackTrace.size, MAX_STACKTRACE_DEPTH))
    }

    private companion object {
      /**
       * Determine what is the max depth of the stacktrace to be taken.
       *
       * Such small stacktrace gives context on local error in the [PublisherCodeRemover] without showing global
       * context with publisher code.
       *
       * The [PublisherCodeRemover] is expected to throw [ReflectiveOperationException] which does not use lot of
       * stacks. So 5 stacks should be sufficient.
       */
      private const val MAX_STACKTRACE_DEPTH = 5
    }
  }
}
