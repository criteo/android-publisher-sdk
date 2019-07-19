package com.criteo.publisher;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.AdUnit;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialTest {

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitial criteoInterstitial;

    @Before
    @UiThreadTest
    public void setup() throws CriteoInitException {
        MockitoAnnotations.initMocks(this);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/None");
        List<AdUnit> cacheAdUnits = new ArrayList<>();
        cacheAdUnits.add(interstitialAdUnit);
        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, "B-056946", cacheAdUnits);
        criteoInterstitial = new CriteoInterstitial(InstrumentationRegistry.getContext(), interstitialAdUnit);
        criteoInterstitial.setCriteoInterstitialAdListener(criteoInterstitialAdListener);
    }

    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        criteoInterstitial.loadAd();

        //wait for the loadAd process to be completed
        Thread.sleep(1000);

        //Expected result , found no slot and called criteoBannerAdListener.onAdFetchFailed
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdReceived();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}