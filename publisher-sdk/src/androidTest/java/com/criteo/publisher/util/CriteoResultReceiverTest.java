package com.criteo.publisher.util;

import static com.criteo.publisher.util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.util.CriteoResultReceiver.ACTION_LEFT_CLICKED;
import static com.criteo.publisher.util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.os.Bundle;
import android.os.Handler;
import com.criteo.publisher.CriteoInterstitialAdListener;
import java.lang.ref.WeakReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoResultReceiverTest {

  private CriteoResultReceiver criteoResultReceiver;

  @Mock
  private WeakReference<CriteoInterstitialAdListener> criteoInterstitialAdListenerRef;

  @Mock
  private CriteoInterstitialAdListener criteoInterstitialAdListener;

  private Bundle bundle;

  @Mock
  private Handler handler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    criteoResultReceiver = new CriteoResultReceiver(
        handler,
        criteoInterstitialAdListenerRef
    );
  }

  @Test
  public void givenListenerReachable_whenSendOnClick_ThenCallbackShouldBeCalled() {
    givenReachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_LEFT_CLICKED));

    verify(criteoInterstitialAdListener, times(1)).onAdLeftApplication();
    verify(criteoInterstitialAdListener, times(1)).onAdClicked();
  }

  @Test
  public void givenListenerReachable_whenSendOnClosed_ThenCallbackShouldBeCalled() {
    givenReachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_CLOSED));

    verify(criteoInterstitialAdListener, times(1)).onAdClosed();
  }

  @Test
  public void givenListenerUnreachable_whenSendOnClick_ThenCallbackShouldNotBeCalled() {
    givenUnreachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_LEFT_CLICKED));
    verifyNoInteractions(criteoInterstitialAdListener);
  }

  @Test
  public void givenListenerUnreachable_whenSendOnClose_ThenCallbackShouldNotBeCalled() {
    givenUnreachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_CLOSED));

    verify(criteoInterstitialAdListener, never()).onAdClosed();
  }

  private Bundle getActionBundle(int actionClosed) {
    bundle = new Bundle();
    bundle.putInt(INTERSTITIAL_ACTION, actionClosed);
    return bundle;
  }

  private void givenReachableListenerReference() {
    when(criteoInterstitialAdListenerRef.get()).thenReturn(criteoInterstitialAdListener);
  }

  private void givenUnreachableListenerReference() {
    when(criteoInterstitialAdListenerRef.get()).thenReturn(null);
  }
}