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

import android.content.SharedPreferences
import com.criteo.publisher.application.InstrumentationUtil
import com.criteo.publisher.mock.DependencyProviderRef
import com.criteo.publisher.mock.TestResource
import com.criteo.publisher.util.SharedPreferencesFactory
import org.mockito.AdditionalAnswers.returnsArgAt
import org.mockito.Answers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever

class SharedPreferencesResource(private val dependencyProviderRef: DependencyProviderRef) : TestResource {
  override fun setUp() {
    if (InstrumentationUtil.isRunningInInstrumentationTest()) {
      return
    }

    dependencyProviderRef.get().inject(SharedPreferencesFactory::class.java, newMock())
  }

  companion object {
    @JvmStatic
    fun newMock(): SharedPreferencesFactory {
      // Explicitly mock shared preferences because it is a pain to handle them in unit tests.
      // This is more true for the getString one, because, since String class is final, it is
      // returning null, and provoke unexpected NPE.
      // Here we consider that all shared preferences are empty. We can still mock them if we want to
      // test scenarios where data is stored.

      // Explicitly mock shared preferences because it is a pain to handle them in unit tests.
      // This is more true for the getString one, because, since String class is final, it is
      // returning null, and provoke unexpected NPE.
      // Here we consider that all shared preferences are empty. We can still mock them if we want to
      // test scenarios where data is stored.
      val sharedPreferences = mock(SharedPreferences::class.java)
      whenever(sharedPreferences.getInt(any(), anyInt())).thenAnswer(returnsArgAt<Int>(1))
      whenever(sharedPreferences.getBoolean(any(), anyBoolean())).thenAnswer(returnsArgAt<Boolean>(1))
      whenever(sharedPreferences.getString(any(), any())).thenAnswer(returnsArgAt<String>(1))

      val editor = mock(SharedPreferences.Editor::class.java, Answers.RETURNS_DEEP_STUBS)
      whenever(sharedPreferences.edit()).thenReturn(editor)

      val factory = mock(SharedPreferencesFactory::class.java)
      whenever(factory.application).thenReturn(sharedPreferences)
      whenever(factory.internal).thenReturn(sharedPreferences)

      return factory
    }
  }
}
