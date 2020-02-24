package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.annotation.NonNull;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.WebViewData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

  @Mock
  private CriteoInterstitialAdListener listener;

  @Mock
  private CriteoInterstitialAdDisplayListener displayListener;

  @Mock
  private WebViewData webViewData;

  @Mock
  private InterstitialActivityHelper interstitialActivityHelper;

  @Mock
  private Criteo criteo;

  @Mock
  private Context context;

  private CriteoInterstitialEventController controller;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    controller = createController();
  }

  @Test
  public void isAdLoaded_GivenLoadedWebViewData_ReturnTrue() throws Exception {
    givenLoadedWebViewData();

    boolean isLoaded = controller.isAdLoaded();

    assertThat(isLoaded).isTrue();
  }

  @Test
  public void isAdLoaded_GivenNotLoadedWebViewData_ReturnFalse() throws Exception {
    when(webViewData.isLoaded()).thenReturn(false);

    boolean isLoaded = controller.isAdLoaded();

    assertThat(isLoaded).isFalse();
  }

  @Test
  public void show_GivenNotLoadedWebViewData_DoesNothing() throws Exception {
    when(webViewData.isLoaded()).thenReturn(false);

    controller.show(context);

    verifyZeroInteractions(context);
    verifyZeroInteractions(listener);
  }

  @Test
  public void show_GivenLoadedWebViewData_NotifyListener() throws Exception {
    givenLoadedWebViewData();

    controller.show(context);

    verify(listener).onAdOpened();
  }

  @Test
  public void show_GivenLoadedWebViewDataAndNullListener_DoesNotCrash() throws Exception {
    givenLoadedWebViewData();
    listener = null;
    controller = createController();

    assertThatCode(() -> controller.show(context)).doesNotThrowAnyException();
  }

  @Test
  public void show_GivenLoadedWebViewData_OpenActivity() throws Exception {
    givenLoadedWebViewData("myContent");

    controller.show(context);

    verify(interstitialActivityHelper).openActivity(context, "myContent", listener);
  }

  @Test
  public void show_GivenLoadedWebViewData_ResetWebViewData() throws Exception {
    givenLoadedWebViewData();

    controller.show(context);

    verify(webViewData).refresh();
  }

  private void givenLoadedWebViewData() {
    givenLoadedWebViewData("ignored");
  }

  private void givenLoadedWebViewData(String webViewContent) {
    when(webViewData.isLoaded()).thenReturn(true);
    when(webViewData.getContent()).thenReturn(webViewContent);
  }

  @NonNull
  private CriteoInterstitialEventController createController() {
    return new CriteoInterstitialEventController(
        listener,
        displayListener,
        webViewData,
        interstitialActivityHelper,
        criteo
    );
  }

}