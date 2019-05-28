package com.criteo.publisher.model;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.criteo.publisher.Util.DeviceUtil;
import java.util.ArrayList;
import java.util.List;

public class InterstitialAdUnits {


    public InterstitialAdUnits(Application application) {

        ArrayList<ScreenSize> screenSizesPortrait = new ArrayList<>();
        screenSizesPortrait.add(new ScreenSize(320, 480));
        screenSizesPortrait.add(new ScreenSize(360, 640));

        ArrayList<ScreenSize> screenSizesLandscape = new ArrayList<>();
        screenSizesLandscape.add(new ScreenSize(480, 320));
        screenSizesLandscape.add(new ScreenSize(640, 360));

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        DeviceUtil.setScreenSize(Math.round(metrics.widthPixels / metrics.density),
                Math.round(metrics.heightPixels / metrics.density), screenSizesPortrait,
                screenSizesLandscape);
    }

    public List<AdUnit> createAdUnits(String placementId) {
        List<AdUnit> retAdUnits = new ArrayList<>();

        int orientation = Configuration.ORIENTATION_PORTRAIT;
        AdUnit interstitialAdUnitPortrait = new AdUnit();
        interstitialAdUnitPortrait.setPlacementId(placementId);
        interstitialAdUnitPortrait.setSizeGivenOrientation(orientation);
        retAdUnits.add(interstitialAdUnitPortrait);

        orientation = Configuration.ORIENTATION_LANDSCAPE;
        AdUnit interstitialAdUnitLandscape = new AdUnit();
        interstitialAdUnitLandscape.setPlacementId(placementId);
        interstitialAdUnitLandscape.setSizeGivenOrientation(orientation);
        retAdUnits.add(interstitialAdUnitLandscape);

        return retAdUnits;
    }

}
