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

import android.content.Context
import com.criteo.publisher.csm.ObjectQueueFactory
import com.criteo.publisher.csm.SendingQueueFactory
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import com.squareup.tape.QueueFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.io.File
import javax.inject.Inject

class RemoteLogObjectQueueFactoryTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  private lateinit var queueFile: File

  @Inject
  private lateinit var context: Context

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @Inject
  private lateinit var configuration: RemoteLogSendingQueueConfiguration

  @Inject
  private lateinit var remoteLogRecordsFactory: RemoteLogRecordsFactory

  private lateinit var factory: ObjectQueueFactory<RemoteLogRecords>

  @Before
  fun setUp() {
    queueFile = File(context.filesDir, configuration.queueFilename)

    factory = spy(ObjectQueueFactory(context, jsonSerializer, configuration)) {
      doReturn(queueFile).whenever(mock).queueFile
    }
  }

  @After
  fun tearDown() {
    queueFile.delete()
  }

  @Test
  fun offer_GivenATonsOfLogs_AcceptAllOfThemButEvictOlderOnesToStayAroundMemoryLimit() {
    val smallSizeEstimationPerLog = 5000
    val maxSize = configuration.maxSizeOfSendingQueue
    val requiredLogsForOverflow = maxSize / smallSizeEstimationPerLog
    val requiredLogsForOverflowWithMargin = (requiredLogsForOverflow * 1.20).toInt()

    val sendingQueue = SendingQueueFactory(factory, configuration).create()

    for (i in 0 until requiredLogsForOverflowWithMargin) {
      val message = "#$i: Lorem ipsum dolor sit amet, consectetur adipiscing elit."

      // Forge an exception that could looks like what is fetch in prod: Exception with cause, suppressed exception
      // which also has a cause. With JUnit framework, those exceptions already have, each, ~30 stacks.
      val cause = Exception("Sed non risus")
      val throwable = Exception(
          "Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor",
          cause
      )
      val suppressedCause = Exception("Cras elementum ultrices diam")
      val suppressed = Exception("Maecenas ligula massa, varius a, semper congue, euismod non, mi", suppressedCause)
      throwable.addSuppressed(suppressed)

      val logRecords = remoteLogRecordsFactory.createLogRecords(LogMessage(
          message = message,
          throwable = throwable
      ))

      sendingQueue.offer(logRecords)
    }

    // The last element can overflow the limit, so we are lenient (up to 1%) on the below condition.
    assertThat(sendingQueue.totalSize * 0.99).isLessThanOrEqualTo(maxSize.toDouble())

    // The queue file grows in power of 2. So it can be, at most, twice larger than expected.
    // To not waste this memory, the max size should be near a power of 2. We are lenient (up to
    // 10%) on this condition.
    assertThat(queueFile.length().toDouble()).isLessThanOrEqualTo(maxSize * 1.10)

    // Verify that the queue can contain, at least, an expected number of elements
    assertThat(QueueFile(queueFile).size()).isGreaterThanOrEqualTo(50)
  }
}
