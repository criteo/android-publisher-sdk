package com.criteo.publisher.advancednative

import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.util.RunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class ImpressionHelperTest {

    @Rule
    @JvmField
    val mockedDependenciesRule = MockedDependenciesRule()

    @MockBean
    private lateinit var api: PubSdkApi

    @SpyBean
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    private val executor = Executor { it.run() }

    @Mock
    private lateinit var runOnUiThreadExecutor: RunOnUiThreadExecutor

    private lateinit var helper: ImpressionHelper

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        helper = ImpressionHelper(api, executor, runOnUiThreadExecutor)
    }

    @Test
    fun firePixels_GivenAPixel_AskToApiToSendItAndCloseStream() {
        val pixels = setOf(URL("http://my.pixel"))
        val stream = mock<InputStream>()
        whenever(api.executeRawGet(any())).thenReturn(stream)

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel"))
        verify(stream).close()
    }

    @Test
    fun firePixels_GivenMultiplePixels_AskToApiToSendThem() {
        val pixels = listOf(
                URL("http://my.pixel.1"),
                URL("http://my.pixel.2")
        )

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel.1"))
        verify(api).executeRawGet(URL("http://my.pixel.2"))
    }

    @Test
    fun firePixels_GivenErrorOnOnePixel_IgnoreItAndContinueOnOthers() {
        whenever(buildConfigWrapper.preconditionThrowsOnException()).doReturn(false)

        val pixels = listOf(
                URL("http://my.pixel.1"),
                URL("http://my.pixel.2"),
                URL("http://my.pixel.3")
        )

        doThrow(IOException::class)
                .doReturn(null)
                .doReturn(mock(InputStream::class.java))
                .whenever(api).executeRawGet(any())

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel.2"))
        verify(api).executeRawGet(URL("http://my.pixel.3"))
    }

    @Test
    fun notifyImpression_GivenListener_NotifyItWithUiExecutor() {
        val isCalledFromExecutor = AtomicBoolean()
        doAnswer {
            isCalledFromExecutor.set(true)
            val runnable = it.arguments[0] as Runnable
            runnable.run()
            isCalledFromExecutor.set(false)
        }.whenever(runOnUiThreadExecutor).executeAsync(any<Runnable>())

        val listener = mock<CriteoNativeAdListener> {
            doAnswer {
                assertThat(isCalledFromExecutor).isTrue
            }.whenever(mock).onAdImpression()
        }

        helper.notifyImpression(listener)

        verify(listener).onAdImpression()
    }

}