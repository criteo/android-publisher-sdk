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

import android.view.View
import android.view.ViewTreeObserver
import com.criteo.publisher.advancednative.VisibilityTracker.VisibilityTrackingTask
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.ref.Reference

class VisibilityTrackingTaskTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var viewRef: Reference<View>

    @Mock
    private lateinit var visibilityChecker: VisibilityChecker

    private lateinit var runOnUiThreadExecutor: RunOnUiThreadExecutor

    @Before
    fun setUp() {
        whenever(viewRef.get()).thenReturn(mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS))
        runOnUiThreadExecutor = spy(DirectMockRunOnUiThreadExecutor())
    }

    @Test
    fun new_GivenEmptyReference_DoNotThrow() {
        whenever(viewRef.get()).thenReturn(null)

        assertThatCode {
            createTask()
        }.doesNotThrowAnyException()
    }

    @Test
    fun new_GivenNotEmptyReferenceAndAliveObserver_SetupPreDrawListener() {
        val viewTreeObserver = givenViewTreeObserver(true)

        val task = createTask()

        verify(viewTreeObserver).addOnPreDrawListener(task)
    }

    @Test
    fun new_GivenNotEmptyReferenceAndNotAliveObserver_DoNotSetupPreDrawListener() {
        val viewTreeObserver = givenViewTreeObserver(false)

        val task = createTask()

        verify(viewTreeObserver, never()).addOnPreDrawListener(task)
    }

    @Test
    fun onPreDraw_GivenListenerAndEmptyReference_DoesNothing() {
        val listener = mock(VisibilityListener::class.java)

        val task = createTask()
        whenever(viewRef.get()).thenReturn(null)

        task.onPreDraw()

        verify(listener, never()).onVisible()
    }

    @Test
    fun onPreDraw_GivenListenerAndReferenceAndIsVisible_NotifyListener() {
        val listener = mock<VisibilityListener>()
        whenever(visibilityChecker.isVisible(viewRef.get()!!)).thenReturn(true)

        val task = createTask()
        task.setListener(listener)
        task.onPreDraw()

        verify(listener).onVisible()
    }

    @Test
    fun onGlobalLayout_GivenListenerAndReferenceAndIsGone_NotifyListener() {
        val listener = mock<VisibilityListener>()
        whenever(visibilityChecker.isVisible(viewRef.get()!!)).thenReturn(false)

        val task = createTask()
        task.setListener(listener)
        task.onGlobalLayout()

        verify(listener).onGone()
    }

    @Test
    fun onPreDraw_GivenListenerAndReferenceAndIsGone_CancelAndAndExecuteAndExecuteAsyncWithDelay() {
        val listener = mock<VisibilityListener>()
        whenever(visibilityChecker.isVisible(viewRef.get()!!)).thenReturn(false)
        givenViewTreeObserver(true)

        val task = createTask()
        task.setListener(listener)
        task.onPreDraw()

        verify(runOnUiThreadExecutor).cancel(any())
        verify(runOnUiThreadExecutor).execute(any())
        verify(runOnUiThreadExecutor).executeAsync(any(), eq(VisibilityTrackingTask.VISIBILITY_POLL_INTERVAL))
    }

    @Test
    fun onPreDraw_GivenListenerAndReferenceAndViewTreeObserverIsNotAlive_ShouldNotExecuteAsyncWithDelay() {
        val listener = mock<VisibilityListener>()
        givenViewTreeObserver(false)

        val task = createTask()
        task.setListener(listener)
        task.onPreDraw()

        verify(runOnUiThreadExecutor).cancel(any())
        verify(runOnUiThreadExecutor, never()).executeAsync(any(), eq(VisibilityTrackingTask.VISIBILITY_POLL_INTERVAL))
    }

    private fun givenViewTreeObserver(isAlive: Boolean): ViewTreeObserver {
        val viewTreeObserver = mock<ViewTreeObserver> {
            on { isAlive() } doReturn isAlive
        }

        whenever(viewRef.get()!!.viewTreeObserver).thenReturn(viewTreeObserver)
        return viewTreeObserver
    }

    private fun createTask(): VisibilityTrackingTask {
        return VisibilityTrackingTask(viewRef, visibilityChecker, runOnUiThreadExecutor)
    }
}
