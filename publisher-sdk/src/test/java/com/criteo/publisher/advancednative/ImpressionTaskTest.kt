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

package com.criteo.publisher.advancednative

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.lang.ref.WeakReference
import java.net.URL

class ImpressionTaskTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var impressionPixels: Iterable<URL>

    @Mock
    private lateinit var helper: ImpressionHelper

    private lateinit var task: ImpressionTask

    @Test
    fun onVisible_GivenPixels_DelegateToHelper() {
        createImpressionTaskWithListener()

        task.onVisible()

        verify(helper).firePixels(impressionPixels)
    }

    @Test
    fun onVisible_GivenEmptyListenerRef_DoesNotThrowAndDoNotDelegateToHelper() {
        createImpressionTaskWithListener()

        assertThatCode {
            task.onVisible()
        }.doesNotThrowAnyException()

        verify(helper, never()).notifyImpression(anyOrNull())
    }

    @Test
    fun onVisible_GivenNotEmptyListenerRef_DelegateToHelper() {
        val listener = mock<CriteoNativeAdListener>()
        createImpressionTaskWithListener(listener)

        task.onVisible()

        verify(helper).notifyImpression(listener)
    }

    @Test
    fun onVisible_CalledTwice_WorkOnlyOnce() {
        val listener = mock<CriteoNativeAdListener>()
        createImpressionTaskWithListener(listener)

        task.onVisible()
        task.onVisible()

        verify(helper, times(1)).firePixels(any())
        verify(helper, times(1)).notifyImpression(any())
    }

    private fun createImpressionTaskWithListener(listener: CriteoNativeAdListener? = null) {
        task = ImpressionTask(impressionPixels, WeakReference(listener), helper)
    }
}
