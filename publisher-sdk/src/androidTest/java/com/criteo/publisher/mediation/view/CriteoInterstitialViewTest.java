package com.criteo.publisher.mediation.view;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CacheAdUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialViewTest {

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialView criteoInterstitialView;

    @Before
    @UiThreadTest
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/None");
        List<AdUnit> cacheAdUnits = new ArrayList<>();
        cacheAdUnits.add(interstitialAdUnit);
        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, cacheAdUnits, "B-056946");
        criteoInterstitialView = new CriteoInterstitialView(InstrumentationRegistry.getContext(), interstitialAdUnit);
        criteoInterstitialView.setCriteoInterstitialAdListener(criteoInterstitialAdListener);
    }

    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        criteoInterstitialView.loadAd();

        //wait for the loadAd process to be completed
        Thread.sleep(1000);

        //Expected result , found no slot and called criteoBannerAdListener.onAdFetchFailed
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}