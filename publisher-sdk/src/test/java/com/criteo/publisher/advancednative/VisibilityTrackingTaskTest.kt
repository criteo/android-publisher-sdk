package com.criteo.publisher.advancednative

import android.view.View
import android.view.ViewTreeObserver
import com.criteo.publisher.advancednative.VisibilityTracker.*
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.lang.ref.Reference

class VisibilityTrackingTaskTest {

    @Mock
    private lateinit var viewRef: Reference<View>

    @Mock
    private lateinit var visibilityChecker: VisibilityChecker

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(viewRef.get()).thenReturn(mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS))
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
    fun onPreDraw_GivenListenerAndReferenceAndVisibilityDetected_NotifyListener() {
        val listener = mock<VisibilityListener>()
        whenever(visibilityChecker.isVisible(viewRef.get()!!)).thenReturn(true)

        val task = createTask()
        task.setListener(listener)
        task.onPreDraw()

        verify(listener).onVisible()
    }

    private fun givenViewTreeObserver(isAlive: Boolean): ViewTreeObserver {
        val viewTreeObserver = mock<ViewTreeObserver> {
            on { isAlive() } doReturn isAlive
        }

        whenever(viewRef.get()!!.viewTreeObserver).thenReturn(viewTreeObserver)
        return viewTreeObserver
    }

    private fun createTask(): VisibilityTrackingTask {
        return VisibilityTrackingTask(viewRef, visibilityChecker)
    }

}