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

import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

class SessionTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var clock: Clock

  @Test
  fun getDuration_GivenSdkInitialized_SessionDurationIsStarted() {
    whenever(clock.currentTimeInMillis).thenReturn(1337L)

    CriteoUtil.givenInitializedCriteo()

    whenever(clock.currentTimeInMillis).thenReturn(2337L)

    val session = DependencyProvider.getInstance().provideSession()
    val duration = session.getDurationInSeconds()

    assertThat(duration).isEqualTo(1)
  }

  @Test
  fun sessionId_GivenSdkInitialized_ReturnAlwaysPerSession() {
    CriteoUtil.givenInitializedCriteo()

    val session1 = DependencyProvider.getInstance().provideSession()
    val sessionId1 = session1.sessionId
    val sessionId2 = session1.sessionId

    mockedDependenciesRule.resetAllDependencies()
    CriteoUtil.givenInitializedCriteo()

    val session2 = DependencyProvider.getInstance().provideSession()
    val sessionId3 = session2.sessionId
    val sessionId4 = session2.sessionId

    assertThat(sessionId1).isEqualTo(sessionId2).isNotEqualTo(sessionId3)
    assertThat(sessionId3).isEqualTo(sessionId4)
  }
}
