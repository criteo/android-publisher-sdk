package com.criteo.publisher;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForMockedBid;
import static org.mockito.Mockito.*;

public class CriteoInterstitialTest {

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitial criteoInterstitial;
    private InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/None");

    @Before
    @UiThreadTest
    public void setup() throws CriteoInitException {
        MockitoAnnotations.initMocks(this);
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
    public void testInHouseLoadAdWithSameAdUnit() throws InterruptedException {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        BidToken token1 = new BidToken(uuid1, interstitialAdUnit);
        criteoInterstitial.loadAd(token1);

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , found no slot and called criteoInterstitialAdListener.onAdFetchFailed
        verify(criteoInterstitialAdListener, times(0)).onAdReceived();
        verify(criteoInterstitialAdListener, times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testInHouseLoadAdWithDifferentAdUnit() throws InterruptedException {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        InterstitialAdUnit interstitialAdUnit2 = new InterstitialAdUnit("/140800857/None2");
        BidToken token1 = new BidToken(uuid1, interstitialAdUnit2);
        criteoInterstitial.loadAd(token1);

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , found no slot and called criteoInterstitialAdListener.onAdFetchFailed
        verify(criteoInterstitialAdListener, never()).onAdReceived();
        verify(criteoInterstitialAdListener, never())
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testInHouseLoadAdWithEqualAdUnitButDifferentInstance() throws InterruptedException {
        UUID uuid = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        InterstitialAdUnit equalAdUnit = new InterstitialAdUnit("/140800857/None");
        BidToken bidToken = new BidToken(uuid, equalAdUnit);

        criteoInterstitial.loadAd(bidToken);

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result, found no slot and called criteoInterstitialAdListener.onAdFetchFailed
        verify(criteoInterstitialAdListener, never()).onAdReceived();
        verify(criteoInterstitialAdListener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        criteoInterstitial.loadAd();

        //wait for the loadAd process to be completed
        Thread.sleep(1000);

        //Expected result , found no slot and called criteoInterstitialAdListener.onAdFetchFailed
        verify(criteoInterstitialAdListener, times(0)).onAdReceived();
        verify(criteoInterstitialAdListener, times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void loadAdForStandaloneTwice_GivenOnlyNoBid_ShouldNotifyListenerTwiceForFailure() throws Exception {
        CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);

        Criteo criteo = mock(Criteo.class);
        when(criteo.getBidForAdUnit(interstitialAdUnit)).thenReturn(null);

        AtomicReference<CriteoInterstitial> interstitial = new AtomicReference<>();

        runOnMainThreadAndWait(() -> {
            CriteoInterstitial interstitialObj = new CriteoInterstitial(
                InstrumentationRegistry.getContext(), interstitialAdUnit, criteo);

            interstitialObj.setCriteoInterstitialAdListener(listener);

            interstitial.set(interstitialObj);
        });

        runOnMainThreadAndWait(interstitial.get()::loadAd);
        waitForMockedBid();

        runOnMainThreadAndWait(interstitial.get()::loadAd);
        waitForMockedBid();

        verify(listener, times(2)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}