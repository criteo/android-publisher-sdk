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

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.criteo.publisher.concurrent.AsyncResources
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.net.URL

class CriteoImageLoaderTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var picasso: Picasso

    @Mock
    private lateinit var requestCreatorAfterLoad: RequestCreator

    @Mock
    private lateinit var requestCreatorAfterPlaceholder: RequestCreator

    @Mock
    private lateinit var requestCreatorAfterFetch: RequestCreator

    @Mock
    private lateinit var asyncResources: AsyncResources

    @InjectMocks
    private lateinit var criteoImageLoader: CriteoImageLoader

    @Mock
    private lateinit var placeholder: Drawable

    @Mock
    private lateinit var imageView: ImageView

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
        verify(requestCreatorAfterPlaceholder).into(eq(imageView), any())
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
