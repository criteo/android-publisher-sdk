package com.criteo.publisher.model;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class AdUnitHelperTest {

    private static final String CRITEO_PUBLISHER_ID = "B-056946";
    private static final BannerAdUnit BANNER_AD_UNIT = new BannerAdUnit("/140800857/Endeavour_320x50",
            new AdSize(320, 50));

    @Test
    public void testCriteoWithANullAdUnit() throws CriteoInitException {
        List<AdUnit> adUnitList = new ArrayList<>();
        adUnitList.add(null);

        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, CRITEO_PUBLISHER_ID, adUnitList);
        Assert.assertNotNull(Criteo.getInstance());
    }

    @Test
    public void testCriteoWithNullAdUnitList() throws CriteoInitException {
        List<AdUnit> adUnitList = null;

        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, CRITEO_PUBLISHER_ID, adUnitList);
        Assert.assertNotNull(Criteo.getInstance());
    }

    @Test
    public void testCriteoWithEmptyAdUnitList() throws CriteoInitException {
        List<AdUnit> adUnitList = new ArrayList<>();

        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, CRITEO_PUBLISHER_ID, adUnitList);
        Assert.assertNotNull(Criteo.getInstance());
    }

    @Test
    public void testCriteoWithAdUnitList() throws CriteoInitException {
        List<AdUnit> adUnitList = new ArrayList<>();
        adUnitList.add(BANNER_AD_UNIT);

        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, CRITEO_PUBLISHER_ID, adUnitList);
        Assert.assertNotNull(Criteo.getInstance());
    }

}
