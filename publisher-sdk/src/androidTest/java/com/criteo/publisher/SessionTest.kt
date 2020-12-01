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
package com.criteo.publisher

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTest {

  @Test
  fun getDuration_GivenSdkInitialized_SessionDurationIsStarted() {
    val clock = mock<Clock>()
    val dependencyProvider = spy(DependencyProvider.getInstance())
    doReturn(clock).whenever(dependencyProvider).provideClock()
    MockableDependencyProvider.setInstance(dependencyProvider)

    whenever(clock.currentTimeInMillis).thenReturn(1337L)

    CriteoUtil.givenInitializedCriteo()

    whenever(clock.currentTimeInMillis).thenReturn(2337L)

    val session = DependencyProvider.getInstance().provideSession()
    val duration = session.getDurationInSeconds()

    assertThat(duration).isEqualTo(1)
  }
}
