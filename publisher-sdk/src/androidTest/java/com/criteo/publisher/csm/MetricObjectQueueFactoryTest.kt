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

package com.criteo.publisher.csm

import android.content.Context
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.JsonSerializer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.tape.QueueFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

class MetricObjectQueueFactoryTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  private lateinit var queueFile: File

  @Inject
  private lateinit var context: Context

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @SpyBean
  private lateinit var configuration: MetricSendingQueueConfiguration

  private lateinit var factory: ObjectQueueFactory<Metric>

  @Before
  fun setUp() {
    whenever(configuration.queueFilename).thenReturn("queueFile")

    queueFile = File(context.filesDir, "queueFile")

    factory = spy(ObjectQueueFactory(context, jsonSerializer, configuration)) {
      doReturn(queueFile).whenever(mock).queueFile
    }
  }

  @After
  fun tearDown() {
    queueFile.delete()
  }

  @Test
  fun offer_GivenATonsOfMetrics_AcceptAllOfThemButEvictOlderOnesToStayAroundMemoryLimit() {
    val smallSizeEstimationPerMetrics = 150
    val maxSize = configuration.maxSizeOfSendingQueue
    val requiredMetricsForOverflow = maxSize / smallSizeEstimationPerMetrics
    val requiredMetricsForOverflowWithMargin = (requiredMetricsForOverflow * 1.20).toInt()

    val sendingQueue = SendingQueueFactory(factory, configuration).create()

    for (i in 0 until requiredMetricsForOverflowWithMargin) {
      val metric = Metric.builder("id$i")
          .setCdbCallStartTimestamp(0L)
          .setCdbCallEndTimestamp(1L)
          .setElapsedTimestamp(2L)
          .build()

      sendingQueue.offer(metric)
    }

    // The last element can overflow the limit, so we are lenient (up to 1%) on the below condition.
    assertThat(sendingQueue.totalSize * 0.99).isLessThanOrEqualTo(maxSize.toDouble())

    // The queue file grows in power of 2. So it can be, at most, twice larger than expected.
    // To not waste this memory, the max size should be near a power of 2. We are lenient (up to
    // 10%) on this condition.
    assertThat(queueFile.length().toDouble()).isLessThanOrEqualTo(maxSize * 1.10)

    // Verify that the queue can contain, at least, an expected number of elements
    assertThat(QueueFile(queueFile).size()).isGreaterThanOrEqualTo(300)
  }
}
