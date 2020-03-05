package com.criteo.publisher.advancednative

import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.net.URI

class ImpressionTaskTest {

    @Mock
    private lateinit var impressionPixels: Iterable<URI>

    @Mock
    private lateinit var helper: ImpressionHelper

    private lateinit var task: ImpressionTask

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        task = ImpressionTask(impressionPixels, helper)
    }

    @Test
    fun onVisible_GivenPixels_DelegateToHelper() {
        task.onVisible()

        verify(helper).firePixels(impressionPixels)
    }

}