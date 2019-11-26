package com.criteo.publisher;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
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

    @Mock
    private Config config;

    @Before
    public void Setup() {
        MockitoAnnotations.initMocks(this);
        criteoBannerEventController = new CriteoBannerEventController(criteoBannerView, criteoBannerAdListener, config);
    }

    @Test
    public void testWithNullAdUnit() throws Exception {
        AdUnit adUnit = null;
        criteoBannerEventController.fetchAdAsync(adUnit);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(1)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }
}
