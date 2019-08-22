package com.criteo.publisher.Util;

import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_LEFT_CLICKED;
import static com.criteo.publisher.Util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.Util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;

import android.os.Bundle;
import android.os.Handler;
import com.criteo.publisher.CriteoInterstitialAdListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoResultReceiverTest {

    private CriteoResultReceiver criteoResultReceiver;

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private Bundle bundle;

    @Mock
    private Handler handler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoResultReceiver = new CriteoResultReceiver(handler,
                criteoInterstitialAdListener);
    }

    @Test
    public void sendOnClick() {
        bundle = new Bundle();
        bundle.putInt(INTERSTITIAL_ACTION, ACTION_LEFT_CLICKED);
        criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, bundle);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1)).onAdLeftApplication();
    }

    @Test
    public void sendOnClose() {
        bundle = new Bundle();
        bundle.putInt(INTERSTITIAL_ACTION, ACTION_CLOSED);
        criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, bundle);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1)).onAdClosed();
    }

    @Test
    public void sendWithNullListener() {
        criteoInterstitialAdListener = null;
        bundle = new Bundle();
        bundle.putInt(INTERSTITIAL_ACTION, ACTION_CLOSED);
        criteoResultReceiver = new CriteoResultReceiver(handler,
                criteoInterstitialAdListener);
        criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, bundle);
    }


}