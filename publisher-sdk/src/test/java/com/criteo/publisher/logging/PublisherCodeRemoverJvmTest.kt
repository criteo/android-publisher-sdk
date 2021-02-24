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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

class PublisherCodeRemoverJvmTest {

  private lateinit var remover: PublisherCodeRemover

  @Before
  fun setUp() {
    remover = PublisherCodeRemover()
  }

  /**
   * This test is not in [PublisherCodeRemoverTest] because D8 remove [Throwable.addSuppressed] and
   * [Throwable.getSuppressed] at compilation time when `minSdkVersion` <= 19, which is currently the case.
   *
   * @see <a href="https://blog.osom.info/2019/10/try-with-resources-desugaring-support.html">Try-with-resources
   * desugaring support in Android's build tools</a>
   */
  @Test
  @Suppress("MaxLineLength", "LongMethod")
  fun removePublisherCode_GivenExceptionWithPublisherCodeAndSdkCode_ReturnExceptionWithOnlySdkCode() {
    val publisherException = IllegalStateException("secret 1")
    publisherException.stackTrace = arrayOf(
        StackTraceElement("com.publisher.FooBar", "foo", "FooBar.java", 1),
        StackTraceElement("com.publisher.FooBar", "bar", "FooBar.java", 2),
        StackTraceElement("android.app.Activity", "onCreate", "Activity.java", 42),
        StackTraceElement("java.lang.Thread", "run", "Thread.java", 1337)
    )

    val sdkException = IllegalArgumentException("sdk message 1")
    sdkException.stackTrace = arrayOf(
        StackTraceElement("com.criteo.mediation.Bar", "foo", "Bar.java", 42),
        StackTraceElement("com.publisher.FooBar", "bar", "FooBar.java", -1),
        StackTraceElement("kotlin.concurrent.ThreadsKt", "thread", "Threads.kt", 1337)
    )

    val thirdPartyOfPublisherCalledFromSdkException = IllegalArgumentException("secret 2")
    thirdPartyOfPublisherCalledFromSdkException.stackTrace = arrayOf(
        StackTraceElement("com.thirdparty.Dummy", "dummy", "Dummy.groovy", -2),
        StackTraceElement("com.publisher.FooBar", "foo", "FooBar.java", 1),
        StackTraceElement("com.criteo.mediation.Bar", "foo", "Bar.java", 42)
    )

    val thirdPartyOfSdkCalledFromPublisherException = RuntimeException("secret 3")
    thirdPartyOfSdkCalledFromPublisherException.stackTrace = arrayOf(
        StackTraceElement("com.squareup.picasso.Picasso", "get", "Picasso.java", 3),
        StackTraceElement("com.publisher.FooBar", "bar", "FooBar.java", 2)
    )

    val thirdPartyOfSdkCalledFromSdkException = IllegalArgumentException("sdk message 2")
    thirdPartyOfSdkCalledFromSdkException.stackTrace = arrayOf(
        StackTraceElement("com.squareup.picasso.Picasso", "get", "Picasso.java", 3),
        StackTraceElement("com.criteo.mediation.Bar", "foo", "Bar.java", 42),
        StackTraceElement("com.publisher.FooBar", "main", "FooBar.java", 2)
    )

    publisherException.initCause(sdkException)
    sdkException.initCause(thirdPartyOfPublisherCalledFromSdkException)
    sdkException.addSuppressed(thirdPartyOfPublisherCalledFromSdkException)
    thirdPartyOfPublisherCalledFromSdkException.initCause(publisherException)
    thirdPartyOfPublisherCalledFromSdkException.addSuppressed(thirdPartyOfSdkCalledFromPublisherException)
    thirdPartyOfSdkCalledFromPublisherException.initCause(thirdPartyOfSdkCalledFromSdkException)
    thirdPartyOfSdkCalledFromSdkException.initCause(sdkException)

    val cleaned = remover.removePublisherCode(publisherException)

    assertThat(cleaned.printStacktraceToString()).isEqualToIgnoringWhitespace("""
      com.criteo.publisher.logging.PublisherCodeRemover${'$'}PublisherException: A IllegalStateException exception occurred from publisher's code
      	at <private class>.<private method>(Unknown Source)
      	at android.app.Activity.onCreate(Activity.java:42)
      	at java.lang.Thread.run(Thread.java:1337)
      Caused by: java.lang.IllegalArgumentException: sdk message 1
      	at com.criteo.mediation.Bar.foo(Bar.java:42)
      	at <private class>.<private method>(Unknown Source)
      	at kotlin.concurrent.ThreadsKt.thread(Threads.kt:1337)
      	Suppressed: com.criteo.publisher.logging.PublisherCodeRemover${'$'}PublisherException: A IllegalArgumentException exception occurred from publisher's code
      		at <private class>.<private method>(Unknown Source)
      		at com.criteo.mediation.Bar.foo(Bar.java:42)
      		Suppressed: com.criteo.publisher.logging.PublisherCodeRemover${'$'}PublisherException: A RuntimeException exception occurred from publisher's code
      			at com.squareup.picasso.Picasso.get(Picasso.java:3)
      			at <private class>.<private method>(Unknown Source)
      		Caused by: java.lang.IllegalArgumentException: sdk message 2
      			at com.squareup.picasso.Picasso.get(Picasso.java:3)
      			at com.criteo.mediation.Bar.foo(Bar.java:42)
      			... 1 more
      		Caused by: [CIRCULAR REFERENCE: java.lang.IllegalArgumentException: sdk message 1]
      	Caused by: [CIRCULAR REFERENCE: com.criteo.publisher.logging.PublisherCodeRemover${'$'}PublisherException: A IllegalStateException exception occurred from publisher's code]
      Caused by: [CIRCULAR REFERENCE: com.criteo.publisher.logging.PublisherCodeRemover${'$'}PublisherException: A IllegalArgumentException exception occurred from publisher's code]
    """.trimIndent())
  }
}
