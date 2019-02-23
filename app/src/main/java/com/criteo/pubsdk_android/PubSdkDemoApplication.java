package com.criteo.pubsdk_android;

import android.app.Application;

import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;

import java.util.ArrayList;
import java.util.List;

public class PubSdkDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
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

        Criteo.init(this, adUnits, 4916);
    }
}
