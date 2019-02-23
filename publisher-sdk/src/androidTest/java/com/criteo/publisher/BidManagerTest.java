package com.criteo.publisher;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.UiThreadTest;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class BidManagerTest {
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    @Test
    @UiThreadTest
    public void testSilentMode() {
        AdUnit adUnit = new AdUnit();
        adUnit.setPlacementId("/140800857/Endeavour_320x50");
        AdSize adSize = new AdSize();
        adSize.setWidth(320);
        adSize.setHeight(50);
        adUnit.setSize(adSize);

        BidManager manager = getInitManager();
        manager.setTimeToNextCall(100);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, adUnit);
        PublisherAdRequest request = manager.enrichBid(builder, adUnit).build();
        assertNull(request.getCustomTargeting().getString("crt_displayUrl"));
    }

    @Test
    @UiThreadTest
    public void testBid() {
        AdUnit adUnit = new AdUnit();
        adUnit.setPlacementId("/140800857/Endeavour_320x50");
        AdSize adSize = new AdSize();
        adSize.setWidth(320);
        adSize.setHeight(50);
        adUnit.setSize(adSize);

        BidManager manager = getInitManager();
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        PublisherAdRequest request = manager.enrichBid(builder, adUnit).build();
        assertNotNull(request.getCustomTargeting().getString("crt_displayUrl"));
    }

    private BidManager getInitManager() {
        List<AdUnit> adUnits = new ArrayList<>();
        AdUnit adUnit = new AdUnit();
        adUnit.setPlacementId("/140800857/Endeavour_320x50");
        AdSize adSize = new AdSize();
        adSize.setWidth(320);
        adSize.setHeight(50);
        adUnit.setSize(adSize);
        adUnits.add(adUnit);

        AdUnit interstitialAdUnit = new AdUnit();
        interstitialAdUnit.setPlacementId("/140800857/Endeavour_Interstitial_320x480");
        AdSize adSizeInterstitial = new AdSize();
        adSizeInterstitial.setWidth(320);
        adSizeInterstitial.setHeight(480);
        interstitialAdUnit.setSize(adSizeInterstitial);
        adUnits.add(interstitialAdUnit);

        Slot slot1 = new Slot();
        slot1.setPlacementId("/140800857/Endeavour_320x50");
        slot1.setHeight(50);
        slot1.setWidth(320);

        Slot slot2 = new Slot();
        slot2.setPlacementId("/140800857/Endeavour_Interstitial_320x480");
        slot2.setHeight(320);
        slot2.setWidth(480);

        List<Slot> slots = new ArrayList<>();
        slots.add(slot1);
        slots.add(slot2);

        BidManager manager = new BidManager(context, 1000, adUnits);
        manager.setAdUnits(slots);
        return manager;
    }

}
