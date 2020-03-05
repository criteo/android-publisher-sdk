package com.criteo.publisher.advancednative

import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.MockitoAnnotations
import java.lang.ref.Reference
import java.net.URI

class ImpressionTaskTest {

    @Mock
    private lateinit var impressionPixels: Iterable<URI>

    @Mock
    private lateinit var listenerRef: Reference<CriteoNativeAdListener>

    @Mock
    private lateinit var helper: ImpressionHelper

    private lateinit var task: ImpressionTask

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        task = ImpressionTask(impressionPixels, listenerRef, helper)
    }

    @Test
    fun onVisible_GivenPixels_DelegateToHelper() {
        task.onVisible()

        verify(helper).firePixels(impressionPixels)
    }

    @Test
    fun onVisible_GivenEmptyListenerRef_DoesNotThrowAndDoNotDelegateToHelper() {
        whenever(listenerRef.get()).thenReturn(null)

        assertThatCode {
            task.onVisible()
        }.doesNotThrowAnyException()

        verify(helper, never()).notifyImpression(anyOrNull())
    }

    @Test
    fun onVisible_GivenNotEmptyListenerRef_DelegateToHelper() {
        val listener = mock<CriteoNativeAdListener>()
        whenever(listenerRef.get()).thenReturn(listener)

        task.onVisible()

        verify(helper).notifyImpression(listener);
    }

    @Test
    fun onVisible_CalledTwice_WorkOnlyOnce() {
        whenever(listenerRef.get()).thenReturn(mock())

        task.onVisible()
        task.onVisible()

        verify(helper, times(1)).firePixels(any())
        verify(helper, times(1)).notifyImpression(any())
    }

}