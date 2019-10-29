package com.criteo.publisher;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class CriteoBannerViewTest {

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    private CriteoBannerView criteoBannerView;

    private BannerAdUnit bannerAdUnit;


    @Before
    @UiThreadTest
    public void setup() throws CriteoInitException {
        MockitoAnnotations.initMocks(this);
        bannerAdUnit = new BannerAdUnit("/140800857/None", new AdSize(320, 50));
        List<AdUnit> AdUnits = new ArrayList<>();
        AdUnits.add(bannerAdUnit);
        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, "9138", AdUnits);
        criteoBannerView = new CriteoBannerView(InstrumentationRegistry.getContext(), bannerAdUnit);
        criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
    }

    @Test
    public void testInHouseLoadAdWithSameAdUnit() throws InterruptedException {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        BidToken token1 = new BidToken(uuid1, bannerAdUnit);
        criteoBannerView.loadAd(token1);

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , found no slot and called criteoBannerAdListener.onAdFetchFailed
        verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testInHouseLoadAdWithDifferentAdUnit() throws InterruptedException {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        BannerAdUnit bannerAdUnit2 = new BannerAdUnit("/140800857/None2", new AdSize(320, 50));
        BidToken token1 = new BidToken(uuid1, bannerAdUnit2);
        criteoBannerView.loadAd(token1);

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , not calling criteoBannerAdListener.onAdReceived and criteoBannerAdListener.onAdFetchFailed
        verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, never())
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testInHouseLoadAdWithEqualAdUnitButDifferentInstance() throws InterruptedException {
        UUID uuid = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        BannerAdUnit equalAdUnit = new BannerAdUnit("/140800857/None", new AdSize(320, 50));
        BidToken bidToken = new BidToken(uuid, equalAdUnit);

        criteoBannerView.loadAd(bidToken);

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , found no slot and called criteoBannerAdListener.onAdFetchFailed
        verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }


    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        criteoBannerView.loadAd();

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , found no slot and called criteoBannerAdListener.onAdFetchFailed
        verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}