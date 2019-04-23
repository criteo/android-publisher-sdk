package com.criteo.publisher.mediation.view;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerViewTest {

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    private CriteoBannerView criteoBannerView;


    @Before
    @UiThreadTest
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AdUnit adUnit = new AdUnit();
        adUnit.setPlacementId("/140800857/None");
        adUnit.setSize(new AdSize(50, 320));
        List<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, adUnits, 4916);
        criteoBannerView = new CriteoBannerView(InstrumentationRegistry.getContext(), adUnit);
        criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
    }


    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        criteoBannerView.loadAd();

        //wait for the loadAd process to be completed
        Thread.sleep(500);

        //Expected result , found no slot and called criteoBannerAdListener.onAdFetchFailed
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdFetchSucceededForBanner(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}