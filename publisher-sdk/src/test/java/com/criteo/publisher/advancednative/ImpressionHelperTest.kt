package com.criteo.publisher.advancednative

import com.criteo.publisher.network.PubSdkApi
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.util.Arrays.asList
import java.util.concurrent.Executor

class ImpressionHelperTest {

    @Mock
    private lateinit var api: PubSdkApi

    private val executor = Executor { it.run() }

    private lateinit var helper: ImpressionHelper

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        helper = ImpressionHelper(api, executor)
    }

    @Test
    fun firePixels_GivenAPixel_AskToApiToSendItAndCloseStream() {
        val pixels = setOf(URI("http://my.pixel"))
        val stream = mock<InputStream>()
        whenever(api.executeRawGet(any())).thenReturn(stream)

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel"))
        verify(stream).close()
    }

    @Test
    fun firePixels_GivenMultiplePixels_AskToApiToSendThem() {
        val pixels = asList(
                URI("http://my.pixel.1"),
                URI("http://my.pixel.2")
        )

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel.1"))
        verify(api).executeRawGet(URL("http://my.pixel.2"))
    }

    @Test
    fun firePixels_GivenNotAUrlPixel_IgnoreItAndContinueOnOthers() {
        val pixels = asList(
                URI("not://a.url"),
                URI("http://my.pixel")
        )

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel"))
    }

    @Test
    fun firePixels_GivenErrorOnOnePixel_IgnoreItAndContinueOnOthers() {
        val pixels = asList(
                URI("http://my.pixel.1"),
                URI("http://my.pixel.2"),
                URI("http://my.pixel.3")
        )

        doThrow(IOException::class)
                .doReturn(null)
                .doReturn(mock(InputStream::class.java))
                .whenever(api).executeRawGet(any())

        helper.firePixels(pixels)

        verify(api).executeRawGet(URL("http://my.pixel.2"))
        verify(api).executeRawGet(URL("http://my.pixel.3"))
    }

}