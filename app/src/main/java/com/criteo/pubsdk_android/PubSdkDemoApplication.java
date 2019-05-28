package com.criteo.pubsdk_android;

import android.app.Application;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnits;
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

        AdUnit moPub = new AdUnit();
        moPub.setPlacementId("b5acf501d2354859941b13030d2d848a");
        AdSize moPubAdSize = new AdSize();
        moPubAdSize.setWidth(320);
        moPubAdSize.setHeight(50);
        moPub.setSize(moPubAdSize);
        adUnits.add(moPub);

        InterstitialAdUnits iAdUnits = new InterstitialAdUnits(this);
        List<AdUnit> interstitialAdUnits = iAdUnits.createAdUnits("/140800857/Endeavour_Interstitial_320x480");
        adUnits.addAll(interstitialAdUnits);

        Criteo.init(this, adUnits, "B-056946");
    }
}
