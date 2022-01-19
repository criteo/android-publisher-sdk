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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class VisibilityCheckerUnitTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var view: View

    private lateinit var checker: VisibilityChecker

    @Before
    fun setUp() {
        checker = VisibilityChecker()
    }

    @Test
    fun isVisible_GivenNotShownView_ReturnFalse() {
        givenVisibleView()
        view.stub {
            on { isShown } doReturn false
        }

        val visible = checker.isVisible(view)

        assertThat(visible).isFalse()
    }

    @Test
    fun isVisible_GivenViewWithoutWidth_ReturnFalse() {
        givenVisibleView()
        view.stub {
            on { width } doReturn 0
        }

        val visible = checker.isVisible(view)

        assertThat(visible).isFalse()
    }

    @Test
    fun isVisible_GivenViewWithoutHeight_ReturnFalse() {
        givenVisibleView()
        view.stub {
            on { height } doReturn 0
        }

        val visible = checker.isVisible(view)

        assertThat(visible).isFalse()
    }

    @Test
    fun isVisible_GivenViewNotVisibleOnScreen_ReturnFalse() {
        givenVisibleView()
        view.stub {
            on { getGlobalVisibleRect(any()) } doReturn false
        }

        val visible = checker.isVisible(view)

        assertThat(visible).isFalse()
    }

    @Test
    fun isVisible_GivenVisibleView_ReturnTrue() {
        givenVisibleView()

        val visible = checker.isVisible(view)

        assertThat(visible).isTrue()
    }

    private fun givenVisibleView() {
        view.stub {
            on { isShown } doReturn true
            on { width } doReturn 1
            on { height } doReturn 1
            on { getGlobalVisibleRect(any()) } doReturn true
        }
    }
}
