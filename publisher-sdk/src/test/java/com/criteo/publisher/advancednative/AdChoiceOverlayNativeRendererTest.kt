package com.criteo.publisher.advancednative

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AdChoiceOverlayNativeRendererTest {

  @Mock
  private lateinit var delegate: CriteoNativeRenderer

  @Mock
  private lateinit var adChoiceOverlay: AdChoiceOverlay

  private lateinit var renderer: CriteoNativeRenderer

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    renderer = AdChoiceOverlayNativeRenderer(delegate, adChoiceOverlay)
  }

  @Test
  fun createNativeView_GivenDelegate_AddAdChoiceOverlayOnDelegateCreation() {
    val context = mock<Context>()
    val parent = mock<ViewGroup>()
    val delegateView = mock<View>()
    val wrappedView = mock<ViewGroup>()

    whenever(delegate.createNativeView(context, parent)).doReturn(delegateView)
    whenever(adChoiceOverlay.addOverlay(delegateView)).doReturn(wrappedView)

    val view = renderer.createNativeView(context, parent)

    assertThat(view).isEqualTo(wrappedView)
  }

  @Test
  fun renderNativeView_GivenDelegateAndFoundInitialView_GiveInitialViewToDelegate() {
    val helper = mock<RendererHelper>()
    val nativeAd = mock<CriteoNativeAd>()
    val nativeView = mock<View>()
    val initialView = mock<View>()

    whenever(adChoiceOverlay.getInitialView(nativeView)).doReturn(initialView)

    renderer.renderNativeView(helper, nativeView, nativeAd)

    verify(delegate).renderNativeView(helper, initialView, nativeAd)
  }

  @Test
  fun renderNativeView_GivenDelegateAndNotFoundInitialView_DoNothing() {
    val helper = mock<RendererHelper>()
    val nativeAd = mock<CriteoNativeAd>()
    val nativeView = mock<View>()

    whenever(adChoiceOverlay.getInitialView(nativeView)).doReturn(null)

    renderer.renderNativeView(helper, nativeView, nativeAd)

    verifyZeroInteractions(delegate)
  }

}