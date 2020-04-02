package com.criteo.publisher.csm

import com.criteo.publisher.Util.BuildConfigWrapper
import com.criteo.publisher.csm.TapeMetricSendingQueue.createFileObjectQueue
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.nhaarman.mockitokotlin2.*
import com.squareup.tape.FileException
import com.squareup.tape.InMemoryObjectQueue
import com.squareup.tape.ObjectQueue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.MockitoAnnotations
import java.io.File
import javax.inject.Inject

@RunWith(Parameterized::class)
class TapeMetricSendingQueueTest(private val tapeImplementation: TapeImplementation) {

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
  private lateinit var metricParser: MetricParser

  private lateinit var tapeQueue: ObjectQueue<Metric>

  private lateinit var queue: TapeMetricSendingQueue

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    tapeQueue = spy(createObjectQueue())
    queue = TapeMetricSendingQueue(tapeQueue)
  }

  private fun createObjectQueue(): ObjectQueue<Metric> {
    return when (tapeImplementation) {
      TapeImplementation.NEW_FILE -> {
        createFileObjectQueueFromNewFile()
      }
      TapeImplementation.EMPTY_QUEUE_FILE -> {
        val newFile = tempFolder.newFile()
        createFileObjectQueueFromNewFile(newFile)
        createFileObjectQueue(newFile, metricParser)
      }
      TapeImplementation.IN_MEMORY -> {
        InMemoryObjectQueue()
      }
    }
  }

  private fun givenMockedTapeQueue() {
    tapeQueue = mock()
    queue = TapeMetricSendingQueue(tapeQueue)
  }

  private fun createFileObjectQueueFromNewFile(newFile: File = tempFolder.newFile()): ObjectQueue<Metric> {
    newFile.delete()
    return createFileObjectQueue(newFile, metricParser)
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

    val size = queue.totalSize

    if (tapeImplementation == TapeImplementation.IN_MEMORY) {
      assertThat(size).isZero()
    } else {
      assertThat(size).isGreaterThan(estimatedSizePerMetric * 200)
    }
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

    tapeQueue.add(metric1)
    tapeQueue.add(metric2)

    val metrics = queue.poll(2)

    assertThat(metrics).containsExactly(metric1, metric2)
    assertThat(tapeQueue.size()).isEqualTo(0)
  }

  @Test
  fun poll_GivenQueueWithNotEnoughCapacity_ReturnListWithOnlyContainedMetrics() {
    val metric1 = mockMetric()

    tapeQueue.add(metric1)

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
    doThrow(FileException::class).whenever(tapeQueue).remove()

    val metrics = queue.poll(1)

    assertThat(metrics).isEmpty()
  }

  private fun givenDeactivatedPreconditionUtils() {
    buildConfigWrapper.stub {
      on { isDebug } doReturn false
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