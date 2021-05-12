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

import com.criteo.publisher.csm.ObjectQueueFactory.AdapterConverter
import com.criteo.publisher.csm.TapeSendingQueueTest.Companion.TapeImplementation.EMPTY_QUEUE_FILE
import com.criteo.publisher.csm.TapeSendingQueueTest.Companion.TapeImplementation.NEW_FILE
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.util.JsonSerializer
import com.squareup.tape.FileException
import com.squareup.tape.FileObjectQueue
import com.squareup.tape.InMemoryObjectQueue
import com.squareup.tape.ObjectQueue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.AdditionalAnswers.delegatesTo
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import javax.inject.Inject


@RunWith(Parameterized::class)
class TapeSendingQueueTest(private val tapeImplementation: TapeImplementation) {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val tempFolder = TemporaryFolder()

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{index}: {0}")
    fun data(): Collection<Array<out Any>> {
      return TapeImplementation.values().toList().map { arrayOf(it) }
    }

    enum class TapeImplementation {
      NEW_FILE,
      EMPTY_QUEUE_FILE,
      IN_MEMORY
    }
  }

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  private lateinit var tapeQueue: ObjectQueue<Metric>

  private lateinit var queue: TapeSendingQueue<Metric>

  @Mock
  private lateinit var objectQueueFactory: ObjectQueueFactory<Metric>

  @Inject
  private lateinit var sendingQueueConfiguration: MetricSendingQueueConfiguration

  private var file: File? = null

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    tapeQueue = spy(createObjectQueue())
    doReturn(tapeQueue).whenever(objectQueueFactory).create()
    queue = TapeSendingQueue(objectQueueFactory, sendingQueueConfiguration)
  }

  @Test
  fun getTotalSize_GivenNewQueue_ReturnASmallSize() {
    val size = queue.totalSize

    // The queue needs a little bit of metadata even in case if empty.
    assertThat(size).isLessThan(20)
  }

  @Test
  fun getTotalSize_AfterFewOperations_ReturnSizeGreaterThanEstimation() {
    val estimatedSizePerMetric = 170

    (0 until 1000).forEach {
      queue.offer(mockMetric(it))
    }

    queue.poll(1000)

    (0 until 200).forEach {
      queue.offer(mockMetric(it))
    }

    // Create new instance of TapeMetricSendingQueue to force recreating the queue reference when polling
    // Since the queue is persistent, we should still find the metrics that were previously saved
    queue = TapeSendingQueue(objectQueueFactory, sendingQueueConfiguration)

    val size = queue.totalSize

    assertThat(size).isGreaterThanOrEqualTo(estimatedSizePerMetric * 200)
  }

  @Test
  fun offer_GivenAcceptedMetric_ReturnTrue() {
    givenMockedTapeQueue()
    val metric = mockMetric()

    doNothing().whenever(tapeQueue).add(any())

    val isOffered = queue.offer(metric)

    verify(tapeQueue).add(metric)
    assertThat(isOffered).isTrue()
  }

  @Test
  fun offer_GivenExceptionWhileAddingMetric_ReturnFalse() {
    givenDeactivatedPreconditionUtils()
    givenMockedTapeQueue()
    val metric = mockMetric()

    doThrow(FileException::class).whenever(tapeQueue).add(any())

    val isOffered = queue.offer(metric)

    verify(tapeQueue).add(metric)
    assertThat(isOffered).isFalse()
  }

  @Test
  fun poll_AfterAnOfferOperation_ReturnOfferedMetric() {
    val metric = mockMetric()

    queue.offer(metric)
    val metrics = queue.poll(1)

    assertThat(metrics).containsExactly(metric)
  }

  @Test
  fun poll_GivenEmptyQueue_ReturnEmptyList() {
    // given empty queue

    val metrics = queue.poll(10)

    assertThat(metrics).isEmpty()
  }

  @Test
  fun poll_GivenZeroMaxElement_ReturnEmptyList() {
    val metrics = queue.poll(0)

    assertThat(metrics).isEmpty()
    verifyZeroInteractions(tapeQueue)
  }

  @Test
  fun poll_GivenQueueWithEnoughCapacity_ReturnListWithFullSize() {
    val metric1 = mockMetric(1)
    val metric2 = mockMetric(2)

    queue.offer(metric1)
    queue.offer(metric2)

    val metrics = queue.poll(2)

    assertThat(metrics).containsExactly(metric1, metric2)
    assertThat(tapeQueue.size()).isEqualTo(0)
  }

  @Test
  fun poll_GivenQueueInstanceOnWhichOfferWasCalledWasRecreated_ReturnListWithFullSize() {
    val metric1 = mockMetric(1)
    val metric2 = mockMetric(2)

    queue.offer(metric1)
    queue.offer(metric2)

    // Create new instance of TapeMetricSendingQueue to force recreating the queue reference when polling
    // Since the queue is persistent, we should still find the metrics that were previously saved
    queue = TapeSendingQueue(objectQueueFactory, sendingQueueConfiguration)

    val metrics = queue.poll(2)

    assertThat(metrics).containsExactly(metric1, metric2)
    assertThat(tapeQueue.size()).isEqualTo(0)
  }

  @Test
  fun poll_GivenQueueWithNotEnoughCapacity_ReturnListWithOnlyContainedMetrics() {
    val metric1 = mockMetric()

    queue.offer(metric1)

    val metrics = queue.poll(2)

    assertThat(metrics).containsExactly(metric1)
    assertThat(tapeQueue.size()).isEqualTo(0)
  }

  @Test
  fun poll_GivenExceptionWhileReadingFromTape_SilenceTheExceptionAndReturnEmptyList() {
    givenDeactivatedPreconditionUtils()
    doThrow(FileException::class).whenever(tapeQueue).peek()

    val metrics = queue.poll(1)

    assertThat(metrics).isEmpty()
  }

  @Test
  fun poll_GivenExceptionWhileRemovingFromTape_SilenceTheExceptionAndReturnEmptyList() {
    givenMockedTapeQueue()
    doReturn(1).whenever(tapeQueue).size()
    doThrow(FileException::class).whenever(tapeQueue).remove()

    val metrics = queue.poll(1)

    assertThat(metrics).isEmpty()
  }

  @Test
  fun poll_GivenZeroByteArrayBeingWritten_RecoverByRemovingBuggyElements() {
    assumeThat(file).isNotNull()

    val metric1 = Metric.builder("id1").build()
    val metric2 = Metric.builder("id2").build()
    val metric3 = Metric.builder("id3").build()
    val metric4 = Metric.builder("id4").build()

    val fileTapeQueue = createFileObjectQueue()
    givenMockedTapeQueue(delegatesTo(fileTapeQueue))
    doAnswer {
      fileTapeQueue.add(it.getArgument(0))
    }.doAnswer {
      // Reproduce bug: bytes full of zero are written
      queue.getQueueFile(fileTapeQueue).add(ByteArray(42))
    }.doAnswer {
      // Reproduce bug: empty byte array
      queue.getQueueFile(fileTapeQueue).add(ByteArray(0))
    }.doAnswer {
      fileTapeQueue.add(it.getArgument(0))
    }.whenever(tapeQueue).add(any())

    val offer1 = queue.offer(metric1) // This writes sane data
    val offer2 = queue.offer(metric2) // This writes buggy data and should be reverted
    val offer3 = queue.offer(metric3) // This writes buggy data and should be reverted
    val offer4 = queue.offer(metric4) // This writes sane data
    val metrics = queue.poll(4)

    assertThat(offer1).isTrue()
    assertThat(offer2).isTrue()
    assertThat(offer3).isTrue()
    assertThat(offer4).isTrue()
    assertThat(metrics).hasSize(2).containsExactly(metric1, metric4)
  }

  @Test
  fun poll_GivenManyWorkersInParallel_ShouldNotProduceDuplicate() {
    for (id in 0 until 2000) {
      val metric = mockMetric(id)
      queue.offer(metric)
    }

    val polledMetric = Collections.newSetFromMap(ConcurrentHashMap<Metric, Boolean>())

    val nbWorkers = 10
    val executor = Executors.newFixedThreadPool(nbWorkers)
    val allAreReadyToWork = CyclicBarrier(nbWorkers)
    val allAreDone = CountDownLatch(nbWorkers)

    for (i in 0 until nbWorkers) {
      executor.execute {
        allAreReadyToWork.await()
        val metrics = queue.poll(100)
        polledMetric.addAll(metrics)
        allAreDone.countDown()
      }
    }

    allAreDone.await()

    assertThat(polledMetric).hasSize(100 * nbWorkers)
  }

  private fun createObjectQueue(): ObjectQueue<Metric> {
    return when (tapeImplementation) {
      NEW_FILE -> {
        file = tempFolder.newFile().apply {
          delete()
        }
        createFileObjectQueue()
      }
      EMPTY_QUEUE_FILE -> {
        file = tempFolder.newFile().apply {
          delete()
        }
        createFileObjectQueue()
        createFileObjectQueue()
      }
      TapeImplementation.IN_MEMORY -> {
        InMemoryObjectQueue()
      }
    }
  }

  private fun createFileObjectQueue() = FileObjectQueue(file, AdapterConverter(jsonSerializer, Metric::class.java))

  private fun givenMockedTapeQueue(defaultAnswer: Answer<Any>? = null) {
    tapeQueue = mock(defaultAnswer = defaultAnswer)
    doReturn(tapeQueue).whenever(objectQueueFactory).create()
    queue = TapeSendingQueue(objectQueueFactory, sendingQueueConfiguration)
  }

  private fun givenDeactivatedPreconditionUtils() {
    buildConfigWrapper.stub {
      on { preconditionThrowsOnException() } doReturn false
    }
  }

  private fun mockMetric(id: Int = 1): Metric {
    return Metric.builder("id$id")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallEndTimestamp(1337L)
        .setElapsedTimestamp(1024L)
        .build()
  }
}
