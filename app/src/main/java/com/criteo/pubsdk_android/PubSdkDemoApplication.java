package com.criteo.pubsdk_android;

import android.app.Application;

import com.criteo.pubsdk.Criteo;
import com.criteo.pubsdk.model.AdSize;
import com.criteo.pubsdk.model.AdUnit;

import java.util.ArrayList;
import java.util.List;

public class PubSdkDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdUnit adUnit = new AdUnit();
        adUnit.setPlacementId("/140800857/Endeavour_320x50");
        AdSize adSize = new AdSize();
        adSize.setWidth(320);
        adSize.setHeight(50);
        adUnit.setSize(adSize);
        List<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);

        Criteo.init(this, adUnits, 4916);
    }
}
