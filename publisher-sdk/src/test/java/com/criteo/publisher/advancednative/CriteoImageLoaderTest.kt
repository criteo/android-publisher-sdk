package com.criteo.publisher.advancednative

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.net.URL

class CriteoImageLoaderTest {
    @Mock
    private lateinit var picasso: Picasso

    @Mock
    private lateinit var requestCreatorAfterLoad: RequestCreator

    @Mock
    private lateinit var requestCreatorAfterPlaceholder: RequestCreator

    @Mock
    private lateinit var requestCreatorAfterFetch: RequestCreator

    @InjectMocks
    private lateinit var criteoImageLoader: CriteoImageLoader

    @Mock
    private lateinit var placeholder: Drawable

    @Mock
    private lateinit var imageView: ImageView

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun loadImageInto(){
        // given
        whenever(picasso.load("http://fake_url")).thenReturn(requestCreatorAfterLoad)
        whenever(requestCreatorAfterLoad.placeholder(placeholder)).thenReturn(requestCreatorAfterPlaceholder)

        // when
        criteoImageLoader.loadImageInto(URL("http://fake_url"), imageView, placeholder)

        // then
        verify(picasso).load("http://fake_url")
        verify(requestCreatorAfterLoad).placeholder(placeholder)
        verify(requestCreatorAfterPlaceholder).into(imageView)
    }

    @Test
    fun preload(){
        // given
        whenever(picasso.load("http://fake_url")).thenReturn(requestCreatorAfterLoad)
        whenever(requestCreatorAfterLoad.placeholder(placeholder)).thenReturn(requestCreatorAfterFetch)

        // when
        criteoImageLoader.preload(URL("http://fake_url"))

        // then
        verify(picasso).load("http://fake_url")
        verify(requestCreatorAfterLoad).fetch()
    }
}
