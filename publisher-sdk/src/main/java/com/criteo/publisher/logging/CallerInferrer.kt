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

import java.lang.reflect.Method

internal object CallerInferrer {

  private const val SdkPackagePrefix = "com.criteo.publisher"

  /**
   * Caller-sensitive operation that infers the name of the caller, skipping `inline` and [Transparent] methods.
   *
   * When called from Kotlin, `inline` works and caller name looks like: `MyEnclosingClass$MyClass#myMethod(String)`.
   *
   * When called from Java, `inline` does not work. Then this fallback on [StackTraceElement] with a best effort to find
   * the caller. If a candidate is found, then its name looks like: `package.MyEnclosingClass$MyClass#myMethod:line`. To
   * reduce length of name, the [SdkPackagePrefix] is trimmed.
   *
   * Note: this method is `inline` because it is annotated with [Transparent]
   */
  @Transparent
  @JvmStatic
  @Suppress("NOTHING_TO_INLINE")
  inline fun inferCallerName(): String? {
    val anonymousObject = object : Any() {}
    val anonymousClass = anonymousObject.javaClass

    // Safe, there is no SecurityException on Android
    val callerMethod = anonymousClass.enclosingMethod ?: return null

    return if (callerMethod.isAnnotationPresent(Transparent::class.java)) {
      inferCallerNameWithStacktrace()
    } else {
      computeCallerName(callerMethod)
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun inferCallerNameWithStacktrace(): String? {
    @Suppress("ThrowableNotThrown")
    val stackTrace = Exception().stackTrace

    // Skip the first element, it is the method which is @Transparent
    // Skipping all @Transparent for real would cost a lot at runtime because we only get the name of classes and
    // methods there. So, as a best effort, we assume that the next element (the 2nd) is the one we're looking for.
    val stackTraceElement = stackTrace.iterator().asSequence().elementAtOrNull(1) ?: return null

    val className = stackTraceElement.className.removePrefix("$SdkPackagePrefix.")
    return "$className#${stackTraceElement.methodName}:${stackTraceElement.lineNumber}"
  }

  private fun computeCallerName(callerMethod: Method): String {
    val parameterNames = callerMethod.parameterTypes.joinToString(", ") {
      it.simpleName
    }

    val packageName = callerMethod.declaringClass.`package`?.name ?: ""
    val className = callerMethod.declaringClass.name.removePrefix("$packageName.")

    return "$className#${callerMethod.name}($parameterNames)"
  }

  /**
   * Indicates that annotated function should be skipped when inferring the caller which is a caller-sensitive
   * operation.
   *
   * Using [Transparent] annotation requires reflection operations which might be costly during runtime. On the other
   * hand, `inline` is done during compilation time and is free during runtime.
   * So Kotlin methods that are [Transparent], should also be `inline`
   *
   * For instance with:
   * ```
   * fun a() = b()
   *
   * @Transparent
   * inline fun b() = c()
   *
   * @Transparent
   * fun c() = /* ... */
   * ```
   * The call to `c` is sensitive to `a` because `b` is inlined in `a`. Note that `b` is also [Transparent].
   */
  @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class Transparent
}
