package com.criteo.pubsdk_android;

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import java.util.ArrayList;
import java.util.List;

public class PubSdkDemoApplication extends Application {

    private static final String TAG = PubSdkDemoApplication.class.getSimpleName();
    public static final String DFP_BANNER_ADUNIT_ID = "/140800857/Endeavour_320x50";
    public static final String DFP_INTERSTITIAL_ADUNIT_ID = "/140800857/Endeavour_Interstitial_320x480";
    public static final String MOPUB_BANNER_ADUNIT_ID = "b5acf501d2354859941b13030d2d848a";
    public static final String MOPUB_INTERSTITIAL_ADUNIT_ID = "86c36b6223ce4730acf52323de3baa93";
    public static final String NATIVE_AD_UNIT_ID = "/140800857/Endeavour_Native";


    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog()
                .build());

        List<AdUnit> adUnits = new ArrayList<>();

        BannerAdUnit bannerAdUnit = new BannerAdUnit(DFP_BANNER_ADUNIT_ID, new AdSize(320, 50));
        adUnits.add(bannerAdUnit);

        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(DFP_INTERSTITIAL_ADUNIT_ID);
        adUnits.add(interstitialAdUnit);

        BannerAdUnit moPubBannerAdUnit = new BannerAdUnit(MOPUB_BANNER_ADUNIT_ID, new AdSize(320, 50));
        adUnits.add(moPubBannerAdUnit);

        InterstitialAdUnit moPubInterstitialAdUnit = new InterstitialAdUnit(MOPUB_INTERSTITIAL_ADUNIT_ID);
        adUnits.add(moPubInterstitialAdUnit);

        try {
            Criteo.init(this, "B-056946", adUnits);
        } catch (Throwable tr) {
            Log.e(TAG, "FAILED TO INIT SDK!!!!", tr);
            throw new IllegalStateException("Criteo SDK is not initialized. You may not proceed.", tr);
        }
    }
}
