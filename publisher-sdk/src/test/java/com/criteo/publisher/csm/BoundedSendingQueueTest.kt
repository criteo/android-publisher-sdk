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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class BoundedSendingQueueTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var delegate: MetricSendingQueue

  @Mock
  private lateinit var sendingQueueConfiguration: SendingQueueConfiguration<Metric>

  @InjectMocks
  private lateinit var queue: BoundedSendingQueue<Metric>

  @Test
  fun poll_GivenDelegate_DelegateToIt() {
    val metric = mock<Metric>()

    delegate.stub {
      on { poll(42) } doReturn listOf(metric)
    }

    val metrics = queue.poll(42)

    assertThat(metrics).containsExactly(metric)
  }

  @Test
  fun getTotalSize_GivenDelegate_DelegateToIt() {
    delegate.stub {
      on { totalSize } doReturn 42
    }

    val size = queue.totalSize

    assertThat(size).isEqualTo(42)
  }

  @Test
  fun offer_GivenDelegateWithSizeBelowThreshold_DelegateToIt() {
    val metric = mock<Metric>()

    delegate.stub {
      on { totalSize } doReturn 42
      on { offer(metric) } doReturn true
    }

    sendingQueueConfiguration.stub {
      on { maxSizeOfSendingQueue } doReturn 1337
    }

    val success = queue.offer(metric)

    assertThat(success).isTrue()
    verify(delegate).offer(metric)
  }

  @Test
  fun offer_GivenDelegateWithMaxCapacity_PollDelegateToMakeRoomAndThenOffer() {
    val metric = mock<Metric>()

    delegate.stub {
      on { totalSize } doReturn 42
      on { offer(metric) } doReturn true
    }

    sendingQueueConfiguration.stub {
      on { maxSizeOfSendingQueue } doReturn 42
    }

    val success = queue.offer(metric)

    assertThat(success).isTrue()
    verify(delegate).offer(metric)
    verify(delegate).poll(1)
  }
}
