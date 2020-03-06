package com.criteo.publisher.advancednative

import android.view.View
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class VisibilityCheckerUnitTest {

    @Mock
    private lateinit var view: View

    private lateinit var checker: VisibilityChecker

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this);

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