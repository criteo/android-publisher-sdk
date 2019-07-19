package com.criteo.publisher;

import com.criteo.publisher.model.AdUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerEventControllerTest {

    private CriteoBannerEventController criteoBannerEventController;

    @Mock
    private CriteoBannerView criteoBannerView;

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Before
    public void Setup() {
        MockitoAnnotations.initMocks(this);
        criteoBannerEventController = new CriteoBannerEventController(criteoBannerView, criteoBannerAdListener);
    }

    @Test
    public void testWithNullAdUnit() throws Exception {
        AdUnit adUnit = null;
        criteoBannerEventController.fetchAdAsync(adUnit);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}