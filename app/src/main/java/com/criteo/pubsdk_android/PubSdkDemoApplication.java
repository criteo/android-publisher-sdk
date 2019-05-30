package com.criteo.pubsdk_android;

import android.app.Application;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.Util.BannerAdUnit;
import com.criteo.publisher.Util.InterstitialAdUnit;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import java.util.ArrayList;
import java.util.List;

public class PubSdkDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        List<AdUnit> adUnits = new ArrayList<>();

        BannerAdUnit bannerAdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(50, 320));
        adUnits.add(bannerAdUnit);

//        BannerAdUnit moPubAdUnit = new BannerAdUnit("b5acf501d2354859941b13030d2d848a");
//        bannerAdUnit.setAdSize(new AdSize(320, 50));
//        adUnits.add(moPubAdUnit);

        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_Interstitial_320x480");
        adUnits.add(interstitialAdUnit);
        Criteo.init(this, adUnits, "B-056946");
    }
}
